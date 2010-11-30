require 'rubygems'
require 'sinatra'
require 'json'
require 'dm-core'
require 'open-uri'

@config = JSON.parse(File.read('config.json'))
@static = @config['static']
$upload = @config['upload']
puts $upload
DataMapper.setup(:default, "sqlite3://#{Dir.pwd}/db.sqlite3")
set :public, @static
set:port, 12599
class Song
  include DataMapper::Resource
  property :id, Serial
  property :name, String
  property :path, String
  property :queued_at, DateTime
  property :is_playing, Boolean, :default  => false
  property :is_queued, Boolean, :default  => false
  property :play_count, Integer, :default => 0
end

class CityStat
  include DataMapper::Resource
  property :id, Serial
  property :city, String
  property :submit_count, Integer, :default => 0
end

class TimeStat
  include DataMapper::Resource
  property :id, Serial
  property :submit_time, Integer
  property :submit_count, Integer, :default => 0
end

DataMapper.auto_upgrade!

get '/' do
  send_file('static/index.html')
end

get '/stats' do
  send_file('static/stats.html')
end

get '/now_playing' do
   song = Song.first(:is_playing => true)
    if song==nil
      content_type :json
         {}
    else
    content_type :json
      {:id => song.id, :name => song.name}.to_json
    end
end

get '/all_songs' do 
  songs = Song.all
  song_list = []
  songs.each do |song|
  song_list = song_list.push({:id => song.id, :name => song.name})
  end
  content_type:json
    song_list.to_json
end

get '/queued_songs' do
    songs = Song.all(:is_queued => true,:order => [:queued_at.asc])
    song_list = []
    songs.each do |song|
    song_list = song_list.push({:id => song.id, :name => song.name, :type => song.type})
    end
    content_type:json
      song_list.to_json
  end 
    
get '/get_song/:id' do |i|
      song = Song.get(i)
      song.is_queued=false
      song.is_playing=true
      song.save
      
      send_file(song.path)
  end
  
get '/track_done' do
   song = Song.first(:is_playing=>true)
    if song != nil
    song.is_playing = false
    song.play_count = song.play_count + 1
    song.save
  end
end
  
post '/upload' do
  mimetype = `file -Ib #{params[:file][:tempfile].path}`.gsub(/\n/,"")
  #server side check to make sure only midi gets through
  if mimetype.include? "audio/midi"
    FileUtils.mv(params[:file][:tempfile].path, "#{$upload}/#{params[:file][:filename]}")
    song = Song.new(:name => params[:name], :path => "#{$upload}/#{params[:file][:filename]}",:queued_at=>Time.now,:is_queued=>true)
    song.save
    save_stats("99.246.119.119")
    #save_stats(@env['REMOTE_ADDR'])
  end
  redirect(back())
end

post '/queue_song' do 
   song = Song.get(params[:value])
   song.queued_at = Time.now
   song.is_queued= true
   song.save
   save_stats("99.246.119.119")
   #save_stats(@env['REMOTE_ADDR'])
end 

get '/top_songs' do 
   songs = Song.all(:limit=>5,:play_count.gt=>0,:order => [:play_count.desc])
   song_list = []
   songs.each do |song|
   song_list = song_list.push({:name => song.name})
   end
   content_type:json
     song_list.to_json
end

get '/song_stats' do 
   songs = Song.all(:play_count.gt=>0)
   song_list = []
   songs.each do |song|
   song_list = song_list.push({:name => song.name,:play_count => song.play_count})
   end
   content_type:json
     song_list.to_json
end

get '/time_stats' do
  stats = TimeStat.all(:submit_count.gt=>0)
  stat_list = []
  stats.each do |stat|
    stat_list = stat_list.push({:submit_time => stat.submit_time,:submit_count=>stat.submit_count})
  end
  content_type:json
     stat_list.to_json
end

get '/city_stats' do
  stats = CityStat.all(:submit_count.gt=>0)
  stat_list = []
  stats.each do |stat|
    stat_list = stat_list.push({:city => stat.city,:submit_count=>stat.submit_count})
  end
  content_type:json
     stat_list.to_json
end

def save_stats(ip)
    #collect where the submitter is from
    contents = open("http://www.geoplugin.net/json.gp?ip=#{ip}") {|io| io.read}
    contents.slice!("geoPlugin(")
    contents.slice!(")")
    #save the city
    city = JSON.parse(contents)['geoplugin_city']
    city_stat = CityStat.first(:city => city)
    puts city_stat
    if city_stat == nil
      city_stat = CityStat.new(:city=>city,:submit_count=>1)
    else
      city_stat.submit_count = city_stat.submit_count + 1
    end
    city_stat.save
    #save the time
    time = Time.new
    #get the the EST time
    time = time.utc - (5 * 60 * 60)
    current_hour = time.hour
    time_stat = TimeStat.first(:submit_time => current_hour)
    if time_stat == nil
      time_stat = TimeStat.new(:submit_time => current_hour, :submit_count => 1)
    else
      time_stat.submit_count = time_stat.submit_count + 1
    end
    time_stat.save
    
end


=begin
get '/delete/song/:name' do |n|
  song = Song.first(:name=>n)
  song.destroy!
end
=end
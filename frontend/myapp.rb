require 'rubygems'
require 'sinatra'
require 'json'
require 'dm-core'


@config = JSON.parse(File.read('config.json'))
$index = @config['static']
$upload = @config['upload']
puts $upload
DataMapper.setup(:default, "sqlite3://#{Dir.pwd}/db.sqlite3")
set :public, @index
set:port, 12599
class Song
  include DataMapper::Resource
  
  property :id, Serial
  property :name, String
  property :path, String
  property :queued_at, DateTime
  property :is_playing, Boolean, :default  => false
  property :is_queued, Boolean, :default  => false
  property :type, String
end

DataMapper.auto_upgrade!

get '/' do
  send_file('index.html')
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
    song.save
  end
end
  
post '/upload' do
  mimetype = `file -Ib #{params[:file][:tempfile].path}`.gsub(/\n/,"")
  
  #audio/mpeg or audio/midi
  if mimetype.include? "audio/mpeg"
    file_type = "mp3"
  end
  if mimetype.include? "audio/midi"
    file_type = "midi"
  end
  
  FileUtils.mv(params[:file][:tempfile].path, "#{$upload}/#{params[:file][:filename]}")
  song = Song.new(:name => params[:name], :path => "#{$upload}/#{params[:file][:filename]}",:queued_at=>Time.now,:is_queued=>true,:type=>file_type)
  song.save
  redirect(back())
end

post '/queue_song' do 
   song = Song.get(params[:value])
   song.queued_at = Time.now
   song.is_queued= true
   song.save
end 
=begin
get '/delete/song/:name' do |n|
  song = Song.first(:name=>n)
  song.destroy!
end
=end
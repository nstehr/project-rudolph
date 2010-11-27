import MidiPlayback
import java.io.File
import LightController
import PhidgetsLights
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT


def controller = getController(true)

def http = new HTTPBuilder('http://localhost:12599/')

//main loop
while(true){

// perform a GET request, expecting JSON response data
//returns the list of queued songs
http.request( GET, JSON ) {
  uri.path = 'queued_songs'
 // response handler for a success response code:
  response.success = { resp, json ->
	println json.responseData.size()
	//for each queued song, download it, and play it  	
	json.each{
		println it.name
		
            wget("http://localhost:12599/get_song/${it.id}","song.mid")
		    def player = new MidiPlayback(controller)	
		    player.sequenceTrack("song.mid")
		    while(MidiPlayback.stillRunning){Thread.sleep(30)}
	    

		//indicate to the server that the song is done
		http.request(GET,TEXT){
			uri.path="/track_done"
			}
		}
  }

  
}
//sleep before starting the whole thing over again....
Thread.sleep(1*60*1000)

}


def getController(testing){
	 def controller	
	//if we are testing, just mock a controller	
	if(testing){
	   controller = [
		initialize: {println "Mock Initialized"},
		doEvent: {event -> println event}
		] as LightController
	}
	else
	  controller = new PhidgetsLights()
	return controller
}

def wget(url,filename){
	def file = new FileOutputStream(filename)
    	def out = new BufferedOutputStream(file)
    	out << new URL(url).openStream()
    	out.close()

}


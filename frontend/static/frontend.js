  // You may specify partial version numbers, such as "1" or "1.3",
  //  with the same result. Doing so will automatically load the 
  //  latest version matching that partial revision pattern 
  //  (i.e. both 1 and 1.3 would load 1.3.2 today).
  google.load("jquery", "1.7.1");

  google.setOnLoadCallback(function() {
    // Place init code here instead of $(document).ready()
	fillSongOptions()
	getQueuedSongs()
	getNowPlaying()
	getTopSongs()
	var refreshId = setInterval(function() {
		getQueuedSongs()
		getNowPlaying()
		getTopSongs()
	}, 3000);
	
	
	$('#selectSubmit').click(function(){
		
		var dataString = "value="+$('#songOptions').val()
		
		$.ajax({
		    url:"queue_song",
			type: "POST",
		    data: dataString,
		    success: function(){
			$("#submitAlert").fadeIn().delay(1000).fadeOut();
			getQueuedSongs()
		}
		    })
		
		
	})
	
	
});

function fillSongOptions(){
//load all the songs via json

$.getJSON("all_songs",
        function(data){
			$.each(data, function(i,item){
				
            	$('#songOptions').append(
				        $('<option></option>').val(item.id).html(item.name)
				    )
          })
        })

}

function getQueuedSongs(){
//load all the songs via json

$.getJSON("queued_songs",
        function(data){
			$('#queueList').find('li').remove()
			$.each(data, function(i,item){
				
            	$('#queueList').append(
				        $('<li></li>').html(item.name)
				    )
          })
        })

}

function getNowPlaying(){
$.getJSON("now_playing",
        
		function(data){
		
		$(".nowPlaying").html(data.name)
        })
}

function getTopSongs(){
    $.getJSON("top_songs",
            function(data){
    			$('#topList').find('li').remove()
    			$.each(data, function(i,item){

                	$('#topList').append(
    				        $('<li></li>').html(item.name)
    				    )
              })
            })
}

function validateFileExtension(fld) {
if(!/(\.mid|\.midi|\.MID|\.MIDI)$/i.test(fld.value)) {
	alert("Invalid file type.");
	fld.form.reset();
	fld.focus();
	return false;
}
return true;
}

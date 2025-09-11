{  	
	let pageOrchestrator = new PageOrchestrator();
    let playlistList = new PlaylistList();
	let uploadSong = new UploadSong();
	let createPlaylist = new CreatePlaylist();
    let showPlaylist = new ShowPlaylist();
    let addSongToPlaylist = new AddSongToPlaylist();
    let theActualPlaylist = new TheActualPlaylist();
    let deletePlaylist = new DeletePlaylist();
    let songPage = new SongPage();
    let playlistOrdinator = new PlaylistOrdinator();
    let logout = new Logout();
	const albumSelect = new AlbumSelect();

	// prepare the view after load is complete
	window.addEventListener("load", () => {
	    if (localStorage.getItem("username") == null) {
	      window.location.href = "index.html";
	    } else {
	      pageOrchestrator.start(); // initialize the components
	    }
	  });
	  
	function PlaylistList() {
	    this.playlistContainer = document.getElementById("playlist_list");
	    this.tableBody = null;

	    this.init = function() {
	        this.close();
	        this.createTableStructure();
	        this.update();
	    };

	    this.close = function() {
	        this.playlistContainer.innerHTML = "";
	        this.playlistContainer.style.display = "none";
	    };

	    this.createTableStructure = function() {
	        const table = document.createElement("table");
	        table.className = "playlist_table";
	        
	        const thead = document.createElement("thead");
	        thead.innerHTML = `
	            <tr>
	                <th>PLAYLIST TITLE</th>
	                <th>CREATION DATE</th>
	                <th>REORDER</th>
	            </tr>
	        `;
	        
	        this.tableBody = document.createElement("tbody");
	        table.appendChild(thead);
	        table.appendChild(this.tableBody);
	        this.playlistContainer.appendChild(table);
	    };

	    this.update = function() {
	        makeCall("GET", 'GetAllPlaylist', null, (req) => {
	            if (req.readyState === XMLHttpRequest.DONE) {
	                switch(req.status) {
	                    case 200:
	                        const playlists = JSON.parse(req.responseText);
	                        this.repaint(playlists);
	                        this.playlistContainer.style.display = "block";
	                        break;
	                    default:
	                        alert("Can't load playlists");
	                        break;
	                }
	            }
	        });
	    };

	    this.repaint = function(playlists) {
			// delete the previous table
			this.tableBody.innerHTML = "";

	        // ADD Playlists
	        playlists.forEach((playlist) => {
	            const row = document.createElement("tr");
				
	           	const titleCell = document.createElement("td");
				titleCell.className ="title_playlist_list";   
				        
				//link the PlaylistPage
				const playlistLink = document.createElement("a");
				playlistLink.href = "#";
				playlistLink.textContent = playlist.title; 
				playlistLink.addEventListener("click", (e) => {
				    e.preventDefault();
				    showPlaylist.show(playlist);

				});
				titleCell.appendChild(playlistLink);
	            
				// column for crationDate
	            const dateCell = document.createElement("td");
	            dateCell.textContent = playlist.creationDate;
	            
	            // Column REORDER
	            const reorderCell = document.createElement("td");
	            const reorderButton = document.createElement("button");
	            reorderButton.textContent = "REORDER";
	            reorderButton.className = "action_button";
	            reorderButton.addEventListener("click", (e) => {
	                e.preventDefault();
	                playlistOrdinator.show(playlist);
	            });
	            reorderCell.appendChild(reorderButton);
	            
				//create the row
	            row.appendChild(titleCell);
	            row.appendChild(dateCell);
	            row.appendChild(reorderCell);
	            
	            this.tableBody.appendChild(row);
	        });
	    };
	}
	
	function UploadSong(){
	    this.uploadsongform = document.getElementById("upload_song_form");

	    this.init = function(){
	        this.uploadsongform.addEventListener("submit", (e) => {
	            e.preventDefault();
	            this.submit();
	        });
	    }
	    
	    this.close = function(){
	        this.uploadsongform.style.display = "none";
	    }

	    this.open = function(){
	        this.uploadsongform.style.display = "block";
	    }

	    this.submit = function(){
	        this.close();
	        makeCall("POST", 'UploadSong', this.uploadsongform, (req) => {
	            if(req.readyState == XMLHttpRequest.DONE){
	                let message = req.responseText;
	                switch(req.status){
	                    case 200:
	                        this.open();
	                        alert("SONG UPLOADED SUCCESSFULLY!!");
	                        albumSelect.loadAlbums();
	                        createPlaylist.show();
	                        break;
	                    default:
	                        this.open();
	                        alert(message);
	                        break;
	                }
	            }
	        });
	    }
	}

	function CreatePlaylist(){
	    this.form = document.getElementById("create_playlist");
	    this.select = document.getElementById("playlist_songs");

	    this.init = function(){
	        this.close();
	        this.show();
	        this.form.addEventListener("submit", (e) => {
	            e.preventDefault();
	            this.submit();
	        });
	    }
	    
	    this.close = function(){
	        this.form.style.display = "none";
	    }
	    
	    this.open = function(){
	        this.form.style.display = "block";
	    }
	    
	    this.show = function(){
	        this.select.innerHTML = ""; // Ripulisci la select ogni volta

	        makeCall("GET", 'GetAllSongs?getAlbumCovers=false', null, (req) => {
	            if (req.readyState == XMLHttpRequest.DONE) {
	                let message = req.responseText;
	                switch(req.status){
	                    case 200:
	                        let songs = JSON.parse(message);
	                        this.update(songs);
	                        break;
	                    default:
	                        alert(message);
	                        break;
	                }
	            }
	        });
	        this.form.style.display = "block";
	    }
	    
	    this.update = function(songs){
	        songs.forEach((song) => {
	            let option = document.createElement("option");
	            option.value = song.ID;
	            option.textContent = "Title " + song.songTitle;
	            this.select.appendChild(option);
	        });
	    }
	    
	    this.submit = function (){
	        this.close();
	        makeCall("POST", 'CreatePlaylist', this.form, (req) => {
	            if (req.readyState == XMLHttpRequest.DONE) {
	                let message = req.responseText;
	                switch(req.status){
	                    case 200:
	                        this.form.reset();
	                        this.open();
	                        playlistList.init();
	                        break;
	                    default:
	                        this.open();
	                        alert(message);
	                        break;
	                }
	            }
	        });
	    }
	}

    function ShowPlaylist(){
        this.playlistPage = document.getElementById("playlist_page");
        this.closePlaylist = document.getElementById("close_playlist");

        this.init = function(){
			//return in the original Home Page when close the current Playlist
            this.closePlaylist.addEventListener("click", (e) => {
                e.preventDefault();
                this.close();
				uploadSong.open();
				createPlaylist.open();
            });
            this.close();
        }
		
        this.reset = function(playlist){
            this.close();
            this.show(playlist);
        }
		
        this.close = function(){
            this.playlistPage.style.display = "none";
			theActualPlaylist.close();
            addSongToPlaylist.close();
            deletePlaylist.close();
        }
		
        this.show = function(playlist){
            this.playlistPage.style.display = "block";
            this.update(playlist);
			uploadSong.close();
			createPlaylist.close();
			playlistOrdinator.closeOrdinator();
			songPage.close();
        }
		
        this.update = function(playlist){  
			theActualPlaylist.reset(playlist);
            addSongToPlaylist.reset(playlist);
            deletePlaylist.reset(playlist);
        }
    }

    function AddSongToPlaylist(){
        this.addsongtoplaylistform = document.getElementById("add_song_to_playlist_form");
        this.addsongtoplaylistformselect = document.getElementById("add_song_to_playlist_form_select");
        this.hiddenplaylistid = document.getElementById("playlistID");
        let currentPlaylist;

        this.init = function(){
            this.close();
            this.addsongtoplaylistform.addEventListener("submit", (e) => {
                e.preventDefault();
                this.submit(currentPlaylist);
            });
        }
		
        this.close = function(){
            this.addsongtoplaylistform.style.display = "none";
        }
        
		this.reset = function(playlist){
            this.addsongtoplaylistformselect.innerHTML = "";
            this.showList(playlist);
        }
        
		//show List Excluded song
		this.showList = function(playlist){
            makeCall("GET", 'GetExcludedSongs?playlistID=' + playlist.ID, null, (req) => {
                if (req.readyState == XMLHttpRequest.DONE) {
                    let message = req.responseText;
                    switch(req.status){
                        case 200:
                            let response = JSON.parse(message);
                            response.forEach((song) => {
                                let option = document.createElement("option");
                                option.value = song.ID;
                                option.textContent = song.songTitle;
                                this.addsongtoplaylistformselect.appendChild(option);
                            });
                            this.hiddenplaylistid.value = playlist.ID;
                            currentPlaylist = playlist;
                            this.addsongtoplaylistform.style.display = "block";
                            break;
                        default:
                            alert(message);
                            break;
                    }
                }
            });
        }
        
		this.submit = function(playlist){
            this.close();
            makeCall("POST", 'AddSongsToPlaylist', this.addsongtoplaylistform, (req) => {
                if (req.readyState == XMLHttpRequest.DONE) {
					let message = req.responseText;
                    switch(req.status){
                        case 200:
                            this.addsongtoplaylistform.reset();
                            showPlaylist.reset(playlist);
                            break;
                        default:
                            alert(message);
                            break;
                    }
                }
            });
        }
    }
	 
    function TheActualPlaylist(){
		this.playlistPageTitle = document.getElementById("playlist_page_title");
        this.pagenumber = document.getElementById("page_number");
        this.emptyplaylist = document.getElementById("empty_playlist");
        this.songstable = document.getElementById("songs_table");
        this.previouspagebutton = document.getElementById("previous_page_button");
        this.nextpagebutton = document.getElementById("next_page_button");
        let currentPage = 1;
        let songList;
        let showingPlaylist;

        this.init = function(){
            this.close();
            this.previouspagebutton.addEventListener("click", (e) => {
                currentPage--;
                this.update(showingPlaylist, songList);
            });
            this.nextpagebutton.addEventListener("click", (e) => {
                currentPage++;
                this.update(showingPlaylist, songList);
            });
        }
		
        this.close = function(){
			this.playlistPageTitle.style.display = "none";
            this.pagenumber.style.display = "none";
            this.emptyplaylist.style.display = "none";
            this.songstable.style.display = "none";
            this.previouspagebutton.style.display = "none";
            this.nextpagebutton.style.display = "none";
            currentPage = 1;
            songList = null;
        }
		
        this.reset = function(playlist){
            this.close();
            this.show(playlist);
        }
		
        this.show = function(playlist){
			this.playlistPageTitle.style.display = "block";
			//set Title
			this.playlistPageTitle.textContent = ("PLAYLIST NAME: "+ playlist.title);
            showingPlaylist = playlist;
			
			//show Songs
            makeCall("GET", 'GetPlaylistSongs?playlistId=' + playlist.ID, null, (req) => {
                if (req.readyState == XMLHttpRequest.DONE) {
                    let message = req.responseText;
                    switch(req.status){
                        case 200:
                            let response = JSON.parse(message);
                            if(response.length > 0){
                                songList = response;
                                currentPage = 1;
                                this.update(playlist, response);
                            } else {
                                this.emptyplaylist.style.display = "block";
                            }
                            break;
                        default:
                            alert(message);
                            break;
                    }
                }
            });
        }
		
        this.update = function(playlist, songs){
            this.pagenumber.textContent = ("Page: " + currentPage);
            this.pagenumber.style.display = "block";

            let fiveSongs = songs.slice((currentPage - 1) * 5, currentPage * 5);

            this.songstable.innerHTML = "";
            fiveSongs.forEach((song) => {
                let th = document.createElement("th");
                let img = document.createElement("img");
                img.src = ("data:image/png;base64," + song.albumImage);
                img.width = 150;
                th.appendChild(img);
				
                let br = document.createElement("br");
                th.appendChild(br);
				
				// title Song linked to SongPage
				const songLink = document.createElement("a");
				songLink.href = (`#`);
				songLink.className = "song_link";
				songLink.textContent = song.songTitle; 
				songLink.addEventListener("click", (e) => {
				    e.preventDefault();
					songPage.show(song.ID, showingPlaylist);
				});
				th.appendChild(songLink);
				
				let br2 = document.createElement("br");
				th.appendChild(br2);
				
				//button remove song from playlist
				const button = document.createElement("button");
				button.textContent = "Remove";
				button.className = "remove-btn";
				button.addEventListener("click", (e) => {
					e.preventDefault();	
					makeCall("POST", `RemoveSongFromPlaylist?songId=${song.ID}&playlistId=${playlist.ID}`, null, (x) => {
				        if (x.readyState === XMLHttpRequest.DONE) {
				            if (x.status === 200) {
				                showPlaylist.reset(playlist);
				            } else {
				                alert("Errore nella rimozione: " + x.responseText);
				            }
				        }
				    });
		    });
				th.appendChild(button);
                this.songstable.appendChild(th);
            });
            this.songstable.style.display = "block";

			// button previous and next controller 
            if(currentPage == 1){
                this.previouspagebutton.style.display = "none";
            }
            else{
                this.previouspagebutton.style.display = "block";
            }
            if(fiveSongs.length < 5 || currentPage * 5 >= songs.length){
                this.nextpagebutton.style.display = "none";
            }
            else{
                this.nextpagebutton.style.display = "block";
            }
        }
    }

    function DeletePlaylist(){
        this.deleteplaylistform = document.getElementById("delete_playlist_form");
		let currentPlaylist; 

        this.init = function(){
            this.close();
            this.deleteplaylistform.addEventListener("submit", (e) => {
                e.preventDefault();
                this.submit();
            });
        }
		
        this.close = function(){
            this.deleteplaylistform.style.display = "none";
        }
		
        this.reset = function(playlist){
            this.close();
            this.show(playlist);
        }
		
        this.show = function(playlist){
            this.deleteplaylistform.style.display = "block";
            this.deleteplaylistform.elements["playlistID"].value = playlist.ID;
			currentPlaylist = playlist;
        }
		
        this.submit = function(){
            this.close();
            makeCall("POST", 'DeletePlaylist? playlistID=' + currentPlaylist.ID, this.deleteplaylistform, (req) => {
                if(req.readyState == XMLHttpRequest.DONE){
					let message = req.responseText;
                    switch(req.status){
                        case 200:
							showPlaylist.close();
							uploadSong.open();
							createPlaylist.open();
							playlistList.init();
                            alert("Playlist deleted successfully!");
                            break;
                        default:
                            alert(message);
                            break;
                    }
                }
            });
        }
    }

   function SongPage() {
	    let container = document.getElementById("song_page");
		let deleteSong = document.getElementById("delete_song");
	    let title = document.getElementById("song_title_page");
	    let genre = document.getElementById("song_genre");
	    let album = document.getElementById("song_album");
	    let artist = document.getElementById("song_singer");
	    let year = document.getElementById("song_publication_year");
	    let cover = document.getElementById("album_image");
	    let audio = document.getElementById("song_audio");
		this.closeSongPage = document.getElementById("close_song_page");
		let currentPlaylist = null;
		let currentSong = null;
					
	    this.init = function () {
	        this.close();
			this.closeSongPage.addEventListener("click", (e) => {
				 e.preventDefault();
				 this.close();
				 showPlaylist.show(currentPlaylist);
			});
			
			deleteSong.addEventListener("click", (e) => {
			    e.preventDefault();
				makeCall("POST", 'DeleteSong?songID=' +currentSong, null, (req) => {
					let message = req.responseText;
			       	if (req.readyState == XMLHttpRequest.DONE) {
			           switch (req.status) {
			           		case 200:
			                    this.close(); // close SongPage
			                    showPlaylist.show(currentPlaylist); //go back to Playlist Page
			                    break;
		                    default:
		                        alert(message);
		                        break;
			                }
			            }
			        });
			  });
	    }

	    this.show = function (songID, playlist) {
			currentPlaylist = playlist; 
			currentSong = songID;
	        makeCall("GET", "GetSong?songId=" + songID, null, (req) => {
	            if (req.readyState === XMLHttpRequest.DONE) {
	                switch (req.status) {
	                    case 200:
							showPlaylist.close();
	                        const song = JSON.parse(req.responseText);
	                        title.textContent ="SONG TITLE:  "+ song.songTitle;
	                        genre.textContent = song.genre;
	                        album.textContent = song.albumTitle;
	                        artist.textContent = song.singer;
	                        year.textContent = song.publicationYear;
	                        cover.src = "data:image/png;base64," + song.albumImage;
	                        audio.src = "data:audio/mpeg;base64," + song.songFilePath;
	                        container.style.display = "block";
	                        break;
	                    default:
	                        alert("Can't load song data.");
	                        break;
	                }
	            }
	        });
	    }

	    this.close = function () {
	        container.style.display = "none";
	        title.textContent = "";
	        genre.textContent = "";
	        album.textContent = "";
	        artist.textContent = "";
	        year.textContent = "";
	        cover.src = "";
	        audio.src = "";
	    }
	}


    function PlaylistOrdinator(){
        let div = document.getElementById("playlist_ordinator");
		this.ordinatorPageTitle = document.getElementById("ordinator_page_title");
        let ordinatortable = document.getElementById("playlist_ordinator_table");
        let closeplaylistordinatorform = document.getElementById("close_ordinator");
        let submitplaylistordinatorform = document.getElementById("submit_ordinator");
        let songList;
        let idList;
        let draggedId;
        let droppedId;
        let playlistIdToReorder;

        this.init = function(){
            this.close();
            div.style.display = "none";
            ordinatortable.style.display = "none";
			
            closeplaylistordinatorform.addEventListener("click", (e) => {
                e.preventDefault();
                this.close();
            });
			
            submitplaylistordinatorform.addEventListener("click", (e) => {
                e.preventDefault();
                this.submit();
            });
        }

        this.close = function(){
            div.style.display = "none";
            ordinatortable.style.display = "none";
            ordinatortable.innerHTML = "";
            songList = null;
            idList = null;
			uploadSong.open();
			createPlaylist.open();
        }
		
		this.closeOrdinator = function(){
   			div.style.display = "none";
			this.ordinatorPageTitle.display = "none";
            ordinatortable.style.display = "none";
            ordinatortable.innerHTML = "";
            songList = null;
            idList = null;
		}
		
        this.show = function(playlist){
            playlistIdToReorder = playlist.ID;
            showPlaylist.close();
			songPage.close();
			uploadSong.close();
			createPlaylist.close();
			
			this.ordinatorPageTitle.style.display = "block";
			this.ordinatorPageTitle.textContent = ("REORDER PLAYLIST TITLE:  "+ playlist.title);
			
            makeCall("GET", 'GetPlaylistSongs?playlistId=' + playlist.ID, null, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            div.style.display = "block";
                            ordinatortable.style.display = "table";
                            let response = JSON.parse(x.responseText);
                            if(response.length > 0){
                                songList = response;
                                idList = songList.map((song) => song.ID);
                                ordinatortable.innerHTML = "";
                                this.rePrint();
                            }
                    }
                }
            });
        }
		
		this.rePrint = function() {
		    ordinatortable.innerHTML = "";

		    songList.forEach((song) => {
		        let tr = document.createElement("tr");
		        let td = document.createElement("td");
		        
		        // Container principale
		        let songContainer = document.createElement("div");
		        songContainer.className = "song_container";

		        // Immagine (non draggable)
		        let img = document.createElement("img");
		        img.src = "data:image/png;base64," + song.albumImage;
		        img.width = 150;
		        img.style.borderRadius = "4px";

		        // Title is DRAGGABLE
		        let titleDiv = document.createElement("div");
		        titleDiv.textContent = song.songTitle;
		        titleDiv.className = "draggable_title";
		        titleDiv.draggable = true;
		       
		        songContainer.appendChild(img);
		        songContainer.appendChild(titleDiv);

		        // Eventi DRAG sul titolo
		        titleDiv.addEventListener("dragstart", (e) => {
		            draggedId = song.ID;
		            e.currentTarget.style.opacity = "0.5";
		            e.currentTarget.style.cursor = "grabbing";
		        });

		        titleDiv.addEventListener("dragend", (e) => {
		            e.currentTarget.style.opacity = "1";
		            e.currentTarget.style.cursor = "grab";
		        });

		        songContainer.addEventListener("dragover", (e) => {
		            e.preventDefault();
		            e.currentTarget.style.backgroundColor = "#e0e0e0";
		        });

		        songContainer.addEventListener("dragleave", (e) => {
		            e.currentTarget.style.backgroundColor = "transparent";
		        });

		        songContainer.addEventListener("drop", (e) => {
		            e.preventDefault();
		            e.currentTarget.style.backgroundColor = "transparent";
		            
		            droppedId = song.ID;
		            
		            if(draggedId && droppedId && draggedId !== droppedId) {
		                const oldIndex = idList.indexOf(draggedId);
		                const newIndex = idList.indexOf(droppedId);
		                
					// change positions
		                [idList[oldIndex], idList[newIndex]] = [idList[newIndex], idList[oldIndex]];
		                [songList[oldIndex], songList[newIndex]] = [songList[newIndex], songList[oldIndex]];
		                
		                this.rePrint();
		            }
		        });

		        td.appendChild(songContainer);
		        tr.appendChild(td);
		        ordinatortable.appendChild(tr);
		    });
		};

        this.submit = function(){

            let formElement = document.createElement("form");

            let inputElement = document.createElement("input");
            inputElement.type = "hidden";
            inputElement.name = "songIds";
            inputElement.value = idList.join(",");
            formElement.appendChild(inputElement);

            makeCall("POST", 'ReorderPlaylistSongs?playlistId=' + playlistIdToReorder, formElement, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            this.close();
                            alert("Playlist reordered!");
                            break;
                        default:
                            alert("Can't reorder playlist");
                            break;
                    }
                }
            });
        }
    }

    function Logout(){
        let logoutbutton = document.getElementById("logout_button");
        let deleteacccountbutton = document.getElementById("delete_account_button");

        this.init = function(){
            logoutbutton.addEventListener("click", (e) => {
                e.preventDefault();
                this.logout();
            });
			
            deleteacccountbutton.addEventListener("click", (e) => {
              	e.preventDefault();
                this.deleteAccount();
            });
        }
		
		//logout button
        this.logout = function(){
            makeCall("POST", 'Logout', null, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            window.location.href = "index.html";
                            localStorage.removeItem("username");
                            break;
                        default:
                            alert("Can't logout");
                            break;
                    }
                }
            });
        }
		
		//deleteAccount button
        this.deleteAccount = function(){
            makeCall("POST", 'DeleteAccount', null, (x) => {
                if(x.readyState == XMLHttpRequest.DONE){
                    switch(x.status){
                        case 200:
                            window.location.href = "index.html";
                            localStorage.removeItem("username");
                            break;
                        default:
                            alert("Can't delete account");
                            break;
                    }
                }
            });
        }
    }
	
	function AlbumSelect() {
	    this.albumSelect = document.getElementById("album_select");
	    this.form = document.getElementById("upload_song_form");
	    let allAlbums = [];

	    this.init = function() {
	        this.loadAlbums();
	    };

	    this.loadAlbums = function() {
	        makeCall("GET", 'AllAlbums', null, (x) => {
	            if (x.readyState === XMLHttpRequest.DONE) {
	                switch(x.status) {
	                    case 200:
	                        allAlbums = JSON.parse(x.responseText);
	                        this.updateAlbumSelect();
	                        break;
	                    default:
	                        console.error("Can't load albums");
	                        break;
	                }
	            }
	        });
	    };

	    this.updateAlbumSelect = function() {
			//selected Album
	        const currentValue = this.albumSelect.value;
	        
			//remove all options except for the first one ("Choose an Existing Album")
	        while (this.albumSelect.options.length > 1) {
	            this.albumSelect.remove(1);
	        }

			//add albums
	        allAlbums.forEach(album => {
	            const option = document.createElement("option");
	            option.value = album.ID;
	            option.textContent = `${album.title}     BY     ${album.singer}`;
	            this.albumSelect.appendChild(option);
	        });
	    };
	}

    function PageOrchestrator(){
        this.start = function(){
            playlistList.init();
			albumSelect.init();
			uploadSong.init();
			createPlaylist.init();
			logout.init();
			showPlaylist.init();
			addSongToPlaylist.init();
			theActualPlaylist.init();
			deletePlaylist.init();
			songPage.init();
			playlistOrdinator.init();
        }
    }
}
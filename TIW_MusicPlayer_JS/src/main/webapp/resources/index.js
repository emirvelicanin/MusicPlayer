// Login Management 
(function() {
	document.getElementById("login_form").addEventListener('submit', (e) => {

		e.preventDefault();

		let form = document.getElementById("login_form");
		let loginButton = document.getElementById("button_login");
		loginButton.disabled = true;
		
		if (form.checkValidity()) {
			makeCall("POST", 'CheckLogin', form,
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {// response has arrived
						let message = req.responseText;
						if(req.status == 200){
							let response = JSON.parse(message);
							localStorage.setItem('username', response.username);
							window.location.href = "Home.html";
						}
						else{
							alert(message);// report the error contained in the response body
							form.reset();
							loginButton.disabled = false;
						}	
					}
				}
			);
		} else {
			form.reportValidity();// trigger the client-side HTML error messaging
		}
	});
})();
	
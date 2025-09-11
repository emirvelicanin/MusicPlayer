// Registration Management 
(function() {
	document.getElementById("check_registration").addEventListener('submit', (e) => {
		e.preventDefault();

		let form = document.getElementById("check_registration");
		let createButton = document.getElementById("button_registration");
		createButton.disabled = true;
		
		if (form.checkValidity()) {
			makeCall("POST", 'CheckRegistration', form,
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {// response has arrived
						let message = req.responseText;
						if(req.status == 200){
							window.location.href = "index.html";
							}
						else{
							alert(message);// report the error contained in the response body
							createButton.disabled = false;
							}	
						}
					}
			);
		} else {
			form.reportValidity();
		}
	});
})();
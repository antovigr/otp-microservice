Usage of: Java, Spring boot
# otp-microservice

Microservice managing One Time Passwords. It creates and validates otp via email.

### POST /otp

Post the following Json formatted information, and the microservice sends an otp token via email, and stores the session just created in a database updated every minute.
It then returns 201

**Format:**

	{
    "preference": "phone or email"
		"address": "email address or phone number",
		"object": "email object",
		"content": "message content (%token% replaced by the otp)",
		"ttl": "time to live in minutes"
	}
  
  
### GET /

Returns 200 if the microservice is working


### POST /otp/{id}

**Format:** otp token

{id} = id of the session linked to an otp 

It validates the token linked to the session id and returns 200 if everything works fine.


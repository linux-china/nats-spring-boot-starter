# publish message to you subject
pub subject="subject1" msg="This is test message":
   nats pub {{subject}} {{msg}}

# subscribe subject
sub subject="minmax.post":
   nats sub {{subject}}

call-min:
  nats request minmax.min "1,2"

call-max:
  nats request minmax.max "1,2"

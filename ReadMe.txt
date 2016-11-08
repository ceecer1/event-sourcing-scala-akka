This code base is splitted into Writeside project for incoming persistent events, queryside project for querying data, rabbitmq for message queuing.

Basic functionality tried to achieve: CQRS Event sourcing approach.

Write side receives Commands, which are validated, processed and then persisted as Events. As the commands are fully processed, the events flow to the query side through rabbitmq channel, and as those events are consumed on query side, those processed events are stored in the persistence mechanism in a query based approach. The query side exposes the the restful apis for querying. Javascript client will know from internal websocket notification that query table is ready which will in turn call the rest api to display the data.

Write side uses Spray, AKKA.
Query side uses Play Framework, Websockets
Rabbitmq server to queue up messages

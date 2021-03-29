import sample_10.module;

service on new module:Listener() {
    remote function onConnect() returns module:ConnectionService {
        return new HelloService();
    }
}

service class HelloService {

}
import sample_10.module as m;

service on new m:Listener() {
    remote function onConnect() returns m:ConnectionService {
        return new HelloService();
    }
}

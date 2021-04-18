public class Listener {

    public isolated function init() returns error? {
        return ();
    }

    public isolated function attach(Service s, () name = ()) returns error? {
        return ();
    }

    public isolated function 'start() returns error? {
        return ();
    }

    public isolated function gracefulStop() returns error? {
        return ();
    }

    public isolated function immediateStop() returns error? {
        return ();
    }

    public isolated function detach(Service s) returns error? {
        return ();
    }
}

public type Service service object {
    remote function onConnect() returns ConnectionService;
};

public type ConnectionService service object {
};

package lab;

import java.util.*;

public final class Router {
    private final List<NetworkInterface> interfaces = new ArrayList<>();

    public int addInterface(NetworkInterface networkInterface) {
        interfaces.add(networkInterface);
        return interfaces.size() - 1;
    }

    public void addRoute(int prefix, int prefixLength, OptionalInt nextHop, int interfaceIndex) {
        throw new UnsupportedOperationException("TODO Router.addRoute");
    }

    public void route() {
        throw new UnsupportedOperationException("TODO Router.route");
    }
}

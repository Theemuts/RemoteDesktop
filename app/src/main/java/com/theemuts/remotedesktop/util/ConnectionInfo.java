package com.theemuts.remotedesktop.util;

import com.theemuts.remotedesktop.exception.InvalidDataException;

/**
 * Created by thomas on 21-9-16.
 */

public class ConnectionInfo {
    private String ip;
    private int clientPort = 36492;
    private int serverPort = 9998;

    public ConnectionInfo(String encoded) throws InvalidDataException {
        String[] ipAndPorts = encoded.split(":");

        switch(ipAndPorts.length) {
            case 0:
                throw new InvalidDataException("The IP address is invalid");
            case 1:
                if(validIP(ipAndPorts[0])) {
                    ip = ipAndPorts[0];
                    break;
                } else {
                    throw new InvalidDataException("The IP address is invalid");
                }
            case 2:
                if(validIP(ipAndPorts[0])) {
                    ip = ipAndPorts[0];
                } else {
                    throw new InvalidDataException("The IP address is invalid");
                }

                switch (validPort(ipAndPorts[1])) {
                    case -1:
                        throw new InvalidDataException("The server port is invalid");
                    case 1:
                        clientPort = Integer.parseInt(ipAndPorts[1]);
                        break;
                    default:
                        break;
                }
                break;
            case 3:
                if(validIP(ipAndPorts[0])) {
                    ip = ipAndPorts[0];
                } else {
                    throw new InvalidDataException("The IP address is invalid");
                }

                switch (validPort(ipAndPorts[1])) {
                    case -1:
                        throw new InvalidDataException("The server port is invalid");
                    case 1:
                        serverPort = Integer.parseInt(ipAndPorts[1]);
                        break;
                    default:
                        break;
                }

                switch (validPort(ipAndPorts[2])) {
                    case -1:
                        throw new InvalidDataException("The client port is invalid");
                    case 1:
                        clientPort = Integer.parseInt(ipAndPorts[2]);
                        break;
                    default:
                        break;
                }
                break;
            default:
                throw new InvalidDataException("The format is invalid, please use \"[IPv4-address:[ServerPort]:[ClientPort]\".");
        }
    }

    public String getIp() {
        return ip;
    }

    public int getClientPort() {
        return clientPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    private static boolean validIP(String ip) {
        try {
            String[] segments = ip.split("\\.");

            boolean valid = segments.length == 4;

            for (String ipSegment : segments) {
                int parsedSegment = Integer.parseInt(ipSegment);
                valid &= (parsedSegment >= 0 & parsedSegment <= 255);
            }

            return valid;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    private static int validPort(String port) {
        if (port.length() == 0) return 0;

        try {
            int parsedPort = Integer.parseInt(port);
            return (parsedPort > 1024 & parsedPort <= 65535) ? 1 : -1;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

package com.WithSecure.dz.models;


import java.util.List;

public class NetworkInterfaceModel
{
    public NetworkInterfaceModel(String name, List<String> ips)
    {
        this.name = name;
        this.ips = ips;
    }

    public final String name;
    public final List<String> ips;
}
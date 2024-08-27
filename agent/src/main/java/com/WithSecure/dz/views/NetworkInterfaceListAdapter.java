package com.WithSecure.dz.views;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.WithSecure.dz.R;
import com.WithSecure.dz.models.NetworkInterfaceModel;

import java.util.List;

public class NetworkInterfaceListAdapter extends ArrayAdapter<NetworkInterfaceModel>
{
    private final Activity context;

    public NetworkInterfaceListAdapter(@NonNull Activity context, List<NetworkInterfaceModel> interfaces) {
        super(context, R.layout.list_view_network_interface, interfaces);

        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent)
    {
        LayoutInflater inflater=this.context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_view_network_interface, null,true);

        TextView name = rowView.findViewById(R.id.interface_name);
        TextView ips = rowView.findViewById(R.id.ips);

        NetworkInterfaceModel data = this.getItem(position);
        assert data != null;

        name.setText(data.name);
        ips.setText(String.join("\n", data.ips));

        return rowView;
    }
}
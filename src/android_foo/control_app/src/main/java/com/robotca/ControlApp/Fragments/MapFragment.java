package com.robotca.ControlApp.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.robotca.ControlApp.Core.RobotGPSSub;
import com.robotca.ControlApp.R;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;



public class MapFragment extends RosFragment implements MapEventsReceiver {

    private RobotGPSSub robotGPSNode;
    MyLocationNewOverlay myLocationOverlay;
    MapView mapView;
    MapEventsOverlay mapEventsOverlay;

    public MapFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, null);
        mapView = (MapView) view.findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);
        mapView.setTileSource(TileSourceFactory.MAPNIK);


        /*try{
            String m_locale =   Locale.getDefault().getDisplayName();
            BingMapTileSource bing = new BingMapTileSource(m_locale);
            Method m = BingMapTileSource.class.getDeclaredMethod("initMetaData");
            m.setAccessible(true);
            m.invoke(this);
            //BingMapTileSource.initMetaData(this);
            BingMapTileSource.retrieveBingKey(this);

            bing.setStyle(BingMapTileSource.IMAGERYSET_AERIAL);
            mapView.setTileSource(bing);
        }
        catch(Exception e){
            e.printStackTrace();
        }*/

        robotGPSNode = new RobotGPSSub();

        myLocationOverlay = new MyLocationNewOverlay(getActivity(), robotGPSNode, mapView);
        mapEventsOverlay = new MapEventsOverlay(mapView.getContext(), this);

        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();

        mapView.getOverlays().add(myLocationOverlay);
        mapView.getOverlays().add(0, mapEventsOverlay);

        IMapController mapViewController = mapView.getController();
        mapViewController.setZoom(18);

        nodeMainExecutor.execute(robotGPSNode, nodeConfiguration.setNodeName("android/ros_gps"));
        return view;
    }

    public void shutdown(){
        nodeMainExecutor.shutdownNodeMain(robotGPSNode);
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint geoPoint) {
        Toast.makeText(mapView.getContext(), "Tapped on (" + geoPoint.getLatitude() + "," + geoPoint.getLongitude() + ")", Toast.LENGTH_LONG).show();
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();

        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint geoPoint) {

        //The (ResourceProxy) this line causes the long press error and I'm not sure why
        //Working on that though
        GroundOverlay myGroundOverlay = new GroundOverlay((ResourceProxy) this);
        myGroundOverlay.setPosition(geoPoint);
        myGroundOverlay.setImage(getResources().getDrawable(R.drawable.marker).mutate());
        myGroundOverlay.setDimensions(75.0f);
        mapView.getOverlays().add(myGroundOverlay);
        Toast.makeText(mapView.getContext(), "Marked on (" + geoPoint.getLatitude() + "," + geoPoint.getLongitude() + ")", Toast.LENGTH_LONG).show();

        //Doesn't seem to be working for the long press
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();

        return true;
    }
}
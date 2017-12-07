package idv.ron.spots.spot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import idv.ron.spots.R;
import idv.ron.spots.main.Common;

public class SpotDetailFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "SpotDetailFragment";

    private GoogleMap map;
    private Spot spot;
    private SpotGetImageTask spotGetImageTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        spot = (Spot) getArguments().getSerializable("spot");
        return inflater.inflate(R.layout.spot_detail_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /* the SupportMapFragment is a child fragment of SpotDetailFragment;
        using getChildFragmentManager() instead of getFragmentManager() */
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().
                        findFragmentById(R.id.fmMap);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
        map.getUiSettings().setZoomControlsEnabled(true);
        if (spot == null) {
            Common.showToast(getActivity(), R.string.msg_NoSpotsFound);
        } else {
            showMap(spot);
        }
    }

    private void showMap(Spot spot) {
        LatLng position = new LatLng(spot.getLatitude(), spot.getLongitude());
        String snippet = getString(R.string.col_Name) + ": " + spot.getName() + "\n" +
                getString(R.string.col_PhoneNo) + ": " + spot.getPhoneNo() + "\n" +
                getString(R.string.col_Address) + ": " + spot.getAddress();

        // focus on the spot
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(9)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);

        // ic_add spot on the map
        map.addMarker(new MarkerOptions()
                .position(position)
                .title(spot.getName())
                .snippet(snippet));

        map.setInfoWindowAdapter(new MyInfoWindowAdapter(getActivity(), spot));
    }

    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View infoWindow;
        private Spot spot;
        private int imageSize;


        MyInfoWindowAdapter(Context context, Spot spot) {
            infoWindow = View.inflate(context, R.layout.spot_detail_infowindow, null);
            this.spot = spot;
            imageSize = getResources().getDisplayMetrics().widthPixels / 3;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            ImageView imageView = infoWindow.findViewById(R.id.imageView);
            String url = Common.URL + "/SpotServlet";
            int id = spot.getId();
            Bitmap bitmap = null;
            try {
                spotGetImageTask = new SpotGetImageTask(url, id, imageSize);
                // passing null and calling get() means not to run FindImageByIdTask.onPostExecute()
                bitmap = spotGetImageTask.execute().get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.default_image);
            }
            TextView tvTitle = infoWindow.findViewById(R.id.tvTitle);
            tvTitle.setText(marker.getTitle());

            TextView tvSnippet = infoWindow.findViewById(R.id.tvSnippet);
            tvSnippet.setText(marker.getSnippet());
            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (spotGetImageTask != null) {
            spotGetImageTask.cancel(true);
        }
    }

}

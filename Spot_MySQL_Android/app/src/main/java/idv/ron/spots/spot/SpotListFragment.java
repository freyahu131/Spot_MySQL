package idv.ron.spots.spot;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import idv.ron.spots.R;
import idv.ron.spots.main.Common;
import idv.ron.spots.main.MyTask;

public class SpotListFragment extends Fragment {
    private static final String TAG = "SpotListFragment";
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvSpots;
    private MyTask spotGetAllTask, spotDeleteTask;
    private SpotGetImageTask spotGetImageTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.spot_list_fragment, container, false);
        swipeRefreshLayout =
                view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                showAllSpots();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        rvSpots = view.findViewById(R.id.rvNews);
        rvSpots.setLayoutManager(new LinearLayoutManager(getActivity()));
        FloatingActionButton btAdd = view.findViewById(R.id.btAdd);
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new SpotInsertFragment();
                switchFragment(fragment);
            }
        });
        return view;
    }

    private void showAllSpots() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/SpotServlet";
            List<Spot> spots = null;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            String jsonOut = jsonObject.toString();
            spotGetAllTask = new MyTask(url, jsonOut);
            try {
                String jsonIn = spotGetAllTask.execute().get();
                Log.d(TAG, jsonIn);
                Type listType = new TypeToken<List<Spot>>(){ }.getType();
                spots = new Gson().fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (spots == null || spots.isEmpty()) {
                Common.showToast(getActivity(), R.string.msg_NoSpotsFound);
            } else {
                rvSpots.setAdapter(new SpotsRecyclerViewAdapter(getActivity(), spots));
            }
        } else {
            Common.showToast(getActivity(), R.string.msg_NoNetwork);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showAllSpots();
    }

    private class SpotsRecyclerViewAdapter extends RecyclerView.Adapter<SpotsRecyclerViewAdapter.MyViewHolder> {
        private LayoutInflater layoutInflater;
        private List<Spot> spots;
        private int imageSize;

        SpotsRecyclerViewAdapter(Context context, List<Spot> spots) {
            layoutInflater = LayoutInflater.from(context);
            this.spots = spots;
            /* 螢幕寬度除以4當作將圖的尺寸 */
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView tvName, tvPhoneNo, tvAddress;

            MyViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivSpot);
                tvName = itemView.findViewById(R.id.tvName);
                tvPhoneNo = itemView.findViewById(R.id.tvPhoneNo);
                tvAddress = itemView.findViewById(R.id.tvAddress);
            }
        }

        @Override
        public int getItemCount() {
            return spots.size();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.spot_recyclerview_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder myViewHolder, int position) {
            final Spot spot = spots.get(position);
            String url = Common.URL + "/SpotServlet";
            int id = spot.getId();
            spotGetImageTask = new SpotGetImageTask(url, id, imageSize, myViewHolder.imageView);
            spotGetImageTask.execute();
            myViewHolder.tvName.setText(spot.getName());
            myViewHolder.tvPhoneNo.setText(spot.getPhoneNo());
            myViewHolder.tvAddress.setText(spot.getAddress());
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Fragment fragment = new SpotDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("spot", spot);
                    fragment.setArguments(bundle);
                    switchFragment(fragment);
                }
            });
            myViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(getActivity(), view, Gravity.END);
                    popupMenu.inflate(R.menu.popup_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.update:
                                    Fragment fragment = new SpotUpdateFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("spot", spot);
                                    fragment.setArguments(bundle);
                                    switchFragment(fragment);
                                    break;
                                case R.id.delete:
                                    if (Common.networkConnected(getActivity())) {
                                        String url = Common.URL + "/SpotServlet";
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.addProperty("action", "spotDelete");
                                        jsonObject.addProperty("spot", new Gson().toJson(spot));
                                        int count = 0;
                                        try {
                                            spotDeleteTask = new MyTask(url, jsonObject.toString());
                                            String result = spotDeleteTask.execute().get();
                                            count = Integer.valueOf(result);
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                        if (count == 0) {
                                            Common.showToast(getActivity(), R.string.msg_DeleteFail);
                                        } else {
                                            spots.remove(spot);
                                            SpotsRecyclerViewAdapter.this.notifyDataSetChanged();
                                            Common.showToast(getActivity(), R.string.msg_DeleteSuccess);
                                        }
                                    } else {
                                        Common.showToast(getActivity(), R.string.msg_NoNetwork);
                                    }
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                    return true;
                }
            });
        }

    }

    private void switchFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.body, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (spotGetAllTask != null) {
            spotGetAllTask.cancel(true);
        }

        if (spotGetImageTask != null) {
            spotGetImageTask.cancel(true);
        }

        if (spotDeleteTask != null) {
            spotDeleteTask.cancel(true);
        }
    }
}

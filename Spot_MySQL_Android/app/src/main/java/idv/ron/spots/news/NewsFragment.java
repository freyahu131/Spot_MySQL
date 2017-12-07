package idv.ron.spots.news;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import idv.ron.spots.R;
import idv.ron.spots.main.Common;
import idv.ron.spots.main.MyTask;

public class NewsFragment extends Fragment {
    private final static String TAG = "NewsFragment";
    private RecyclerView rvNews;
    private MyTask newsGetAllTask;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);
        rvNews = view.findViewById(R.id.rvNews);
        rvNews.setLayoutManager(new LinearLayoutManager(getActivity()));
        showAllNews();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showAllNews();
    }

    private void showAllNews() {
        if (Common.networkConnected(getActivity())) {
            String url = Common.URL + "/NewsServlet";
            List<News> newsList = null;
            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action", "getAll");
                String jsonOut = jsonObject.toString();
                newsGetAllTask = new MyTask(url, jsonOut);
                String jsonIn = newsGetAllTask.execute().get();
                Log.d(TAG, jsonIn);
                Gson gson = new Gson();
                Type listType = new TypeToken<List<News>>(){ }.getType();
                newsList = gson.fromJson(jsonIn, listType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (newsList == null || newsList.isEmpty()) {
                Common.showToast(getActivity(), R.string.msg_NoNewsFound);
            } else {
                rvNews.setAdapter(new NewsRecyclerViewAdapter(getActivity(), newsList));
            }
        } else {
            Common.showToast(getActivity(), R.string.msg_NoNetwork);
        }
    }

    private class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {
        private LayoutInflater layoutInflater;
        private List<News> newsList;
        private boolean[] newsExpanded;

        NewsRecyclerViewAdapter(Context context, List<News> newsList) {
            layoutInflater = LayoutInflater.from(context);
            this.newsList = newsList;
            newsExpanded = new boolean[newsList.size()];
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNewsTitle, tvNewsDetail;

            ViewHolder(View itemView) {
                super(itemView);
                tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
                tvNewsDetail = itemView.findViewById(R.id.tvNewsDetail);
            }
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.news_recyclerview_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int position) {
            News news = newsList.get(position);
            String title = news.getFormatedDate() + " " + news.getTitle();
            viewHolder.tvNewsTitle.setText(title);
            viewHolder.tvNewsDetail.setText(news.getDetail());
            viewHolder.tvNewsDetail.setVisibility(
                    newsExpanded[position] ? View.VISIBLE : View.GONE);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    expand(viewHolder.getAdapterPosition());
                }
            });
        }

        private void expand(int position) {
            // 被點擊的資料列才會彈出內容，其他資料列的內容會自動縮起來
            // for (int i=0; i<newsExpanded.length; i++) {
            // newsExpanded[i] = false;
            // }
            // newsExpanded[position] = true;

            newsExpanded[position] = !newsExpanded[position];
            notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (newsGetAllTask != null) {
            newsGetAllTask.cancel(true);
        }
    }

}
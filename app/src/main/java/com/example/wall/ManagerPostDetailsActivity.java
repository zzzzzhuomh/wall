package com.example.wall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.example.wall.ui.view.SwipeRefresh;
import com.example.wall.ui.view.SwipeRefreshLayout;
import com.example.wall.ui.vo.CommentForPost;
import com.example.wall.ui.vo.Posts;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManagerPostDetailsActivity extends BaseActivity {

    private String post_id;
    ImageView Back_Image;
    TextView post_title;

    SwipeRefreshLayout eSwipeRefreshLayout;
    RecyclerView eRecyclerView;
    List<CommentForPost> CommentsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_post_details);
        Intent intent = getIntent();
        post_id = intent.getStringExtra("post_id");
        setTitle("帖子详情");
        initView();
        initEvent();
        get_post_comment(post_id);

    }

    public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_SPECIAL = 0; // 特殊Item的视图类型
        private static final int VIEW_TYPE_NORMAL = 1; // 普通Item的视图类型

        private List<CommentForPost> comments;
        private Posts inner_post;

        public CommentAdapter(List<CommentForPost> comments, Posts inner_post) {
            this.comments = comments;
            this.inner_post = inner_post;
        }

        public void setSpecialComment(Posts inner_post) {
            this.inner_post = inner_post;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0 && inner_post != null) {
                return VIEW_TYPE_SPECIAL; // 返回特殊Item的视图类型
            } else {
                return VIEW_TYPE_NORMAL; // 返回普通Item的视图类型
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_TYPE_SPECIAL) {
                // 创建特殊Item的ViewHolder
                View specialView = inflater.inflate(R.layout.item_inner_post, parent, false);
                return new SpecialCommentViewHolder(specialView);
            } else {
                // 创建普通Item的ViewHolder
                View normalView = inflater.inflate(R.layout.item_comment_manager, parent, false);
                return new NormalCommentViewHolder(normalView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof SpecialCommentViewHolder) {
                // 绑定特殊Item的数据到ViewHolder
                SpecialCommentViewHolder specialHolder = (SpecialCommentViewHolder) holder;
                specialHolder.bind(inner_post);
            } else if (holder instanceof NormalCommentViewHolder) {
                // 绑定普通Item的数据到ViewHolder
                int commentPosition = position;
                if (inner_post != null) {
                    // 减去特殊Item的位置偏移量
                    commentPosition--;
                }
                CommentForPost comment = comments.get(commentPosition);
                NormalCommentViewHolder normalHolder = (NormalCommentViewHolder) holder;
                normalHolder.bind(comment);
                normalHolder.delete_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View vp = (View) v.getParent();
                        View vpp = (View) vp.getParent();
                        View vppp = (View) vpp.getParent();
                        // 从视图的tag属性中获取ID
                        String comment_id = (String) vppp.getTag(R.id.id_m_comment_id);
                        Log.d("todeid",comment_id);
                        OkHttpClient client = new OkHttpClient();
                        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://192.168.0.124:8086/api/comment/delete").newBuilder();
                        //urlBuilder.addQueryParameter("post_id", this_id);
                        String url = urlBuilder.build().toString();

                        RequestBody requestBody = new FormBody.Builder()
                                .add("comment_id", comment_id)
                                .build();

                        Request request = new Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build();

                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                //请求失败
                                Log.i("请求情况：", "请求失败");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (response.isSuccessful()) {
                                    Log.i("响应状态", "响应成功");
                                    assert response.body() != null;
                                    final String responseBody = response.body().string();
                                    Log.i("回复", responseBody);
                                    Gson gson = new Gson();
                                    get_post_comment(post_id);
                                }
                            }
                        });
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            int count = comments.size();
            if (inner_post != null) {
                count++; // 如果特殊Item存在，则增加一个Item数量
            }
            return count;
        }

        // 特殊Item的ViewHolder
        private class SpecialCommentViewHolder extends RecyclerView.ViewHolder {
            // 定义特殊Item的视图组件
            private final TextView contentTextView;
            private final ImageView contentImageView;
            private final TextView ownerTextView;
            private final TextView post_time_view;
            public SpecialCommentViewHolder(View itemView) {
                super(itemView);
                // 初始化特殊Item的视图组件
                contentTextView = itemView.findViewById(R.id.id_m_in_post_content);
                contentImageView = itemView.findViewById(R.id.id_m_in_post_image);
                ownerTextView = itemView.findViewById(R.id.id_m_in_post_author);
                post_time_view = itemView.findViewById(R.id.id_m_in_post_time);
            }

            public void bind(Posts inner_post) {
                // 根据特殊Item的数据设置视图组件的内容
                contentTextView.setText(inner_post.getContext());
                if(inner_post.getContent_type() == 1) {
                    String url = inner_post.getMedia_url();
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(url)
                            .override(1000, 1000)//图片大小
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                                    Drawable drawable = new BitmapDrawable(getApplicationContext().getResources(), resource);
                                    contentImageView.setImageDrawable(drawable);
                                    contentImageView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }


                            });
                }
                ownerTextView.setText(inner_post.getOwner());
                post_time_view.setText(inner_post.getTime());
            }
        }

        // 普通Item的ViewHolder
        private class NormalCommentViewHolder extends RecyclerView.ViewHolder {
            // 定义普通Item的视图组件
            private final TextView contentTextView;
            private final ImageView contentImageView;
            private final TextView ownerTextView;
            private final TextView post_time_view;
            public Button delete_button;
            public NormalCommentViewHolder(View itemView) {
                super(itemView);
                contentTextView = itemView.findViewById(R.id.id_m_comment_content);
                contentImageView = itemView.findViewById(R.id.id_m_comment_image);
                ownerTextView = itemView.findViewById(R.id.id_m_comment_author);
                delete_button = itemView.findViewById(R.id.id_m_comment_delete);
                post_time_view = itemView.findViewById(R.id.id_m_comment_post_time);
            }

            public void bind(CommentForPost comment) {
                contentTextView.setText(comment.getContext());
                if(comment.getContent_type() == 1) {
                    String url = comment.getMedia_url();
                    Glide.with(getApplicationContext())
                            .asBitmap()
                            .load(url)
                            .override(1000, 1000)//图片大小
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                                    Drawable drawable = new BitmapDrawable(getApplicationContext().getResources(), resource);
                                    contentImageView.setImageDrawable(drawable);
                                    contentImageView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }


                            });
                }
                ownerTextView.setText(comment.getOwner());
                post_time_view.setText(comment.getDeliver_time());
                itemView.setTag(R.id.id_m_comment_id, comment.getComment_id());
            }
        }
    }

    private void get_post_comment(String post_id) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://192.168.0.124:8086/api/post").newBuilder();
        urlBuilder.addQueryParameter("post_id", post_id);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //请求失败
                Log.i("请求情况：", "请求失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("响应状态", "响应成功");
                    assert response.body() != null;
                    final String responseBody = response.body().string();
                    Gson gson = new Gson();
                    final PostResponse postResponse = gson.fromJson(responseBody, PostResponse.class);
                    final Posts this_post = postResponse.getData();

                    // 获取评论
                    OkHttpClient client = new OkHttpClient();
                    HttpUrl.Builder urlBuilder = HttpUrl.parse("http://192.168.0.124:8086/api/comment").newBuilder();
                    urlBuilder.addQueryParameter("page_num", "1");
                    urlBuilder.addQueryParameter("page_size", String.valueOf(1000000));
                    urlBuilder.addQueryParameter("post_id", post_id);
                    String url = urlBuilder.build().toString();

                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Call call1 = client.newCall(request);
                    call1.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call1, IOException e) {
                            //请求失败
                            Log.i("请求情况：", "请求失败");
                        }

                        @Override
                        public void onResponse(Call call1, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                Log.i("响应状态", "响应成功");
                                assert response.body() != null;
                                final String responseBody = response.body().string();
                                Gson gson = new Gson();
                                final ReadCommentsResponse commentsResponse = gson.fromJson(responseBody, ReadCommentsResponse.class);

                                // 在主线程中更新UI
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (commentsResponse.getCode() == 0) {
                                            post_title.setText(this_post.getTitle());
                                            InnerCommentsData comments = commentsResponse.getData(); // 解析从后端获取的回复数据，得到评论列表
                                            // 创建适配器并设置给 RecyclerView
                                            CommentAdapter adapter = new CommentAdapter(comments.getComments(), this_post);
                                            eRecyclerView.setAdapter(adapter);
                                            // 设置布局管理器，可以选择线性布局、网格布局等
                                            eRecyclerView.setLayoutManager(new LinearLayoutManager(ManagerPostDetailsActivity.this)); // 使用线性布局
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
    private void initView() {
        Back_Image = findViewById(R.id.id_m_back_list);
        post_title = findViewById(R.id.id_m_detail_title);
        eSwipeRefreshLayout = findViewById(R.id.id_swiperefresh_m_inpost);
        eRecyclerView = findViewById(R.id.id_recyclerview_m_inpost);
        CommentsList = new ArrayList<>();
        eSwipeRefreshLayout.setMode(SwipeRefresh.Mode.BOTH);
        eSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLACK, Color.YELLOW, Color.GREEN);
        eRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
    private void initEvent() {
        Back_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toHomeActivity();
            }
        });
        eSwipeRefreshLayout.setOnRefreshListener(new SwipeRefresh.OnRefreshListener() {
            @Override
            public void onRefresh() {
                get_post_comment(post_id);
                eSwipeRefreshLayout.setRefreshing(false);
                eSwipeRefreshLayout.setPullUpRefreshing(false);
            }
        });

        eSwipeRefreshLayout.setOnPullUpRefreshListener(new SwipeRefreshLayout.OnPullUpRefreshListener() {

            @Override
            public void onPullUpRefresh() {
                eSwipeRefreshLayout.setRefreshing(false);
                eSwipeRefreshLayout.setPullUpRefreshing(false);
            }
        });


    }
}
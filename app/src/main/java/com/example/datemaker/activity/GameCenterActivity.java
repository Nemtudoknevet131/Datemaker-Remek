package com.example.datemaker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datemaker.R;
import com.example.datemaker.adapter.GameAdapter;
import com.example.datemaker.adapter.GameItem;
import com.example.datemaker.databinding.ActivityGameCenterBinding;
import com.example.datemaker.game.FlappyActivity;
import com.example.datemaker.game.IceHockeyActivity;
import com.example.datemaker.game.SnakeActivity;
import com.example.datemaker.game.TankTroubleActivity;
import com.example.datemaker.game.FireboyWatergirlActivity;
import com.example.datemaker.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.List;

public class GameCenterActivity extends AppCompatActivity {

    private ActivityGameCenterBinding binding;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameCenterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecycleView();
    }

    private void setupRecycleView() {
        RecyclerView recyclerView = binding.gameRecyclerView;

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        List<GameItem> games = new ArrayList<>();

        games.add(new GameItem("Flappy Bird", R.drawable.flappy_bird));
        games.add(new GameItem("Ice Hockey", R.drawable.glow_hockey));
        games.add(new GameItem("Snake", R.drawable.snake_game));
        games.add(new GameItem("Tank Trouble", R.drawable.tank_icon));
        games.add(new GameItem("Fireboy & Watergirl", R.drawable.fireboy_and_watergirl_fixed));
        games.add(new GameItem("Love Arrows", R.drawable.love_arrows_icon));

        GameAdapter adapter = new GameAdapter(games, new GameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GameItem item) {
                if ("Flappy Bird".equals(item.getTitle())) {
                    Intent intent = new Intent(GameCenterActivity.this, FlappyActivity.class);

                    UserSessionManager session = new UserSessionManager(GameCenterActivity.this);
                    String avatarUrl = session.getAvatarUrl();
                    intent.putExtra("AVATAR_URL", session.getAvatarUrl());

                    startActivity(intent);
                    return;
                }

                if ("Ice Hockey".equals(item.getTitle())) {
                    Intent intent = new Intent(GameCenterActivity.this, IceHockeyActivity.class);
                    startActivity(intent);
                    return;
                }

                if ("Tank Trouble".equals(item.getTitle())) {
                    Intent intent = new Intent(GameCenterActivity.this, TankTroubleActivity.class);
                    startActivity(intent);
                    return;
                }

                if ("Snake".equals(item.getTitle())) {
                    Intent intent = new Intent(GameCenterActivity.this, SnakeActivity.class);
                    startActivity(intent);
                    return;
                }

                if ("Fireboy & Watergirl".equals(item.getTitle())) {
                    Intent intent = new Intent(GameCenterActivity.this, FireboyWatergirlActivity.class);
                    startActivity(intent);
                    return;
                }

                if ("Love Arrows".equals(item.getTitle())) {
                    Intent intent = new Intent(GameCenterActivity.this, LoveArrowsGameActivity.class);
                    startActivity(intent);
                    return;
                }

                Toast.makeText(GameCenterActivity.this, "Clicked: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

            int bottom = Math.max(systemBars.bottom, ime.bottom);

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    bottom
            );

            return insets;
        });
    }
}

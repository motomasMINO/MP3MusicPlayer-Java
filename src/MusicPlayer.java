import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.ArrayList;

public class MusicPlayer extends PlaybackListener {
    // isPausedをより同期的に更新
    private static final Object playSignal = new Object();

    // このクラスのGUIを更新できるように参照
    private MusicPlayerGUI musicPlayerGUI;

    // 楽曲の詳細を保存する楽曲クラス
    private Song currentSong;
    public Song getCurrentSong() {
        return currentSong;
    }

    private ArrayList<Song> playlist;

    // プレイリスト内のインデックスを追跡
    private int currentPlaylistIndex;

    // JLayerライブラリを使用して、曲の再生を処理するAdvancedPlayer objを作成
    private AdvancedPlayer advancedPlayer;

    // プレーヤーが一時停止されているかどうかを示すboolean
    private boolean isPaused;

    // 曲が終了したことを示すboolean
    private boolean songFinished;

    private boolean pressedNext, pressedPrev;

    // 再生が終了したときに失われたフレームを保存(一時停止と再開に使用)
    private int currentFrame;
    public void setCurrentFrame(int frame) {
        currentFrame = frame;
    }

    // 曲を再生してから何ミリ秒経過したかを追跡(スライダーの更新に使用)
    private int currentTimeInMilli;
    public void setCurrentTimeMilli(int timeInMilli) {
        currentTimeInMilli = timeInMilli;
    }

    // コンストラクタ
    public MusicPlayer(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
    }

    public void loadSong(Song song) {
        currentSong = song;
        playlist = null;
        
        // 可能な場合、曲を止める
        if(!songFinished)
            stopSong();

        // nullでない場合は現在の曲を再生する
        if(currentSong != null) {
            // フレームをリセット
            currentFrame = 0;

            // 現在の時間をミリ単位でリセット
            currentTimeInMilli = 0;

            // GUIを更新
            musicPlayerGUI.setPlaybackSliderValue(0);

            playCurrentSong();
        }
    }

    public void loadPlaylist(File playlistFile) {
        playlist = new ArrayList<>();

        // テキストファイルからのパスをプレイリストの配列リストに保存
        try {
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // テキストファイルの各行にアクセスし、テキストをsongPath変数に格納
            String songPath;
            while((songPath = bufferedReader.readLine()) != null) {
                // 曲のパスに基づいて曲オブジェクトを作成
                Song song = new Song(songPath);

                // プレイリストの配列リストに追加
                playlist.add(song);
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        if(playlist.size() > 0) {
            // プレイバックスライダーをリセット
            musicPlayerGUI.setPlaybackSliderValue(0);
            currentTimeInMilli = 0;

            // 現在の曲をプレイリストの最初の曲に更新
            currentSong = playlist.get(0);

            // 最初のフレームから開始
            currentFrame = 0;

            // GUIを更新
            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.updateSongTitleAndArtist(currentSong);
            musicPlayerGUI.updatePlaybackSlider(currentSong);

            // 曲を開始
            playCurrentSong();
        }
    }

    public void pauseSong() {
        if(advancedPlayer != null) {
            // isPausedフラグを更新
            isPaused = true;

            // プレーヤーを止める
            stopSong();
        }
    }

    public void stopSong() {
        if(advancedPlayer != null) {
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void nextSong() {
        // プレイリストがない場合は、次の曲に進まない
        if(playlist == null) return;

        // プレイリストの最後まで到達したかどうかを確認 (到達した場合は何も行いません)
        if(currentPlaylistIndex + 1 > playlist.size() - 1) return;

        pressedNext = true;    

        // 可能な場合、曲を止める
        if(!songFinished)
            stopSong();

        // 現在のプレイリストのインデックスを増やす
        currentPlaylistIndex++;

        // 現在の曲を更新
        currentSong = playlist.get(currentPlaylistIndex);

        // フレームをリセット
        currentFrame = 0;

        // 現在の時間をミリ秒単位でリセット
        currentTimeInMilli = 0;

        // GUIを更新
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        // 曲を再生
        playCurrentSong();
    }

    public void prevSong() {
        // プレイリストがない場合は、次の曲に進まない
        if(playlist == null) return;

        // 前の曲に戻れるか確認する
        if(currentPlaylistIndex - 1 < 0) return;

        pressedPrev = true;    

        // 可能な場合、曲を止める
        if(!songFinished)
            stopSong();

        // 現在のプレイリストのインデックスを減らす
        currentPlaylistIndex--;

        // 現在の曲を更新
        currentSong = playlist.get(currentPlaylistIndex);

        // フレームをリセット
        currentFrame = 0;

        // 現在の時間をミリ秒単位でリセット
        currentTimeInMilli = 0;

        // GUIを更新
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        // 曲を再生
        playCurrentSong();
    }

    public void playCurrentSong() {
        if(currentSong == null) return;

        try {
            // mp3オーディオデータを読み込む
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // 新しいadvanced playerを作成
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            // 曲を再生
            startMusicThread();

            // プレイバックスライダースレッドを開始
            startPlaybackSliderThread();

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    // 曲の再生を処理するスレッドを作成
    private void startMusicThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(isPaused) {
                        synchronized(playSignal) {
                            // フラグを更新
                            isPaused = false;

                            // 他のスレッドに継続を通知(isPausedが適切にfalseに更新されていることを確認)
                            playSignal.notify();
                        }

                        // 失われたフレームから曲を再開
                        advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                    }else {
                        // 冒頭から曲を再生
                        advancedPlayer.play();
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // スライダーの更新を処理するスレッドを作成
    private void startPlaybackSliderThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(isPaused) {
                    try {
                        // 他のスレッドから通知されるまで待機
                        // 続行する前にisPausedのブーリアンフラグがfalseに更新されることを確認
                        synchronized(playSignal) {
                            playSignal.wait();
                        }
                     }catch(Exception e) {
                         e.printStackTrace();
                     }
                 }

                 while(!isPaused && !songFinished && !pressedNext && !pressedPrev) {
                     try {
                        // 現在の時間をミリ秒単位で増加
                        currentTimeInMilli++;

                        // フレーム値に計算
                        int calculatedFrame = (int) ((double) currentTimeInMilli * 2.08 * currentSong.getFrameRatePerMilliseconds());

                        // GUIを更新
                        musicPlayerGUI.setPlaybackSliderValue(calculatedFrame);

                        //thread.sleepを使用して1ミリ秒を模倣
                        Thread.sleep(1);
                      }catch(Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
         }).start();
     }
    
    @Override
    public void playbackStarted(PlaybackEvent evt) {
        // このメソッドは曲が始まったときに呼び出されます
        //System.out.println("Playback Started");
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        // このメソッドは曲の終わりまたはプレーヤーが閉じられたときに呼び出されます
        //System.out.println("Playback Finished");
        if(isPaused) {
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        }else {
            // ユーザーが"次へ"または"前へ"を押した場合、残りのコードを実行しない
            if(pressedNext || pressedPrev) return;

            // 曲が終わるとき
            songFinished = true;

            if(playlist == null) {
                // GUIを更新
                musicPlayerGUI.enablePlayButtonDisablePauseButton();
            }else {
                //プレイリストの最後の曲
                if(currentPlaylistIndex == playlist.size() - 1) {
                    // GUIを更新
                    musicPlayerGUI.enablePlayButtonDisablePauseButton();
                }else {
                    // プレイリストの次の曲へ進む
                    nextSong();
                }
            }
        }
    }
}
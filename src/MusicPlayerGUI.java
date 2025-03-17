import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

public class MusicPlayerGUI extends JFrame {
    // 色を設定
    public static final Color FRAME_COLOR = Color.GREEN;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;

    // アプリでファイルエクスプローラーの使用を許可
    private JFileChooser jFileChooser;

    private JLabel songTitle, songArtist;
    private JPanel playbackBtns;
    private JSlider playbackSlider;

    public MusicPlayerGUI() {
        // JFrameコンストラクタを呼び出してGUIを構成し、タイトルヘッダーを"MP3オーディオプレーヤー"に設定
        super("MP3ミュージックプレーヤー");

        // 幅と高さを設定
        setSize(400, 600);

        // アプリを閉じるとプログラムが終了
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 画面中央でアプリを起動
        setLocationRelativeTo(null);

        // ウィンドウサイズ変更不可
        setResizable(false);

        // レイアウトをnullに設定してコンポーネントの(x, y)座標を制御する
        // 高さと幅も設定できるようにする
        setLayout(null);

        // フレーム色を変更
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();

        // ファイルエクスプローラーのデフォルトパスを設定
        jFileChooser.setCurrentDirectory(new File("src/assets"));

        // ファイル選択フィルターを使用して、.mp3ファイルのみを表示する
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();
    }

    private void addGuiComponents() {
        // ツールバーを追加
        addToolbar();

        // 音符画像読み込み
        JLabel songImage = new JLabel(loadImage("src/music-notes.png"));
        songImage.setBounds(0, 50, getWidth() - 20, 225); 
        add(songImage);

        // 曲のタイトル
        songTitle = new JLabel("タイトル");
        songTitle.setBounds(0, 285, getWidth() - 10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);
        
        // 曲のアーティスト
        songArtist = new JLabel("アーティスト");
        songArtist.setBounds(0, 315, getWidth() - 10, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);

        // プレイバックスライダー
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ユーザーがtickを押しているときに曲を一時停止する
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // ユーザーがtickをドロップするとき
                JSlider source = (JSlider) e.getSource();

                // ユーザーが再生したいフレーム値を取得
                int frame = source.getValue();

                // 音楽プレーヤーの現在のフレームをこのフレームに更新
                musicPlayer.setCurrentFrame(frame);

                // 現在の時間をミリ秒単位で更新
                musicPlayer.setCurrentTimeMilli((int) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));

                // 曲を再開
                musicPlayer.playCurrentSong();

                // 一時停止ボタンをオンにして再生ボタンをオフにする
                enablePauseButtonDisablePlayButton();
            }
        });
        add(playbackSlider);

        // プレイバックボタン(前へ, 再生, 一時停止, 次へ)
        addPlaybackBtns();
    }

    private void addToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);

        // ツールバーが移動しないようにする
        toolBar.setFloatable(false);

        // ドロップダウンメニューを追加
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        //楽曲メニューを追加
        JMenu songMenu = new JMenu("ソング");
        menuBar.add(songMenu);

        // ソングメニューに楽曲ロード項目を追加
        JMenuItem loadSong = new JMenuItem("楽曲をロード");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ユーザーが何を行ったかを伝える整数が返される
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                // ユーザーが"開く"ボタンを押したかどうかを確認
                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    // 選択したファイルに基づいて曲のオブジェクトを作成
                    Song song = new Song(selectedFile.getPath());

                    // アプリ内で曲を読み込む
                    musicPlayer.loadSong(song);

                    // 曲名とアーティスト名を更新
                    updateSongTitleAndArtist(song);

                    // プレイバックスライダーを更新
                    updatePlaybackSlider(song);

                    // 一時停止ボタンをオンにして再生ボタンをオフにする
                    enablePauseButtonDisablePlayButton();
                }
            }
        });
        songMenu.add(loadSong);

        // プレイリストメニューを追加
        JMenu playlistMenu = new JMenu("プレイリスト");
        menuBar.add(playlistMenu);

        // プレイリストメニューにアイテムを追加
        JMenuItem createPlaylist = new JMenuItem("プレイリスト作成");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // プレイリストダイアログを読み込む
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("プレイリストをロード");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("プレイリスト", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    // 曲を停止
                    musicPlayer.stopSong();

                    // プレイリストをロード
                    musicPlayer.loadPlaylist(selectedFile);
                }
            }
        });
        playlistMenu.add(loadPlaylist);

        add(toolBar);
    }

    private void addPlaybackBtns() {
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth() - 10, 80);
        playbackBtns.setBackground(null);

        // "前へ"ボタン
        JButton prevButton = new JButton(loadImage("src/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 前の曲へ戻る
                musicPlayer.prevSong();
            }
        });
        playbackBtns.add(prevButton);

        // "再生"ボタン
        JButton playButton = new JButton(loadImage("src/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 再生ボタンをオフにして一時停止ボタンをオンにする
                enablePauseButtonDisablePlayButton();

                // 曲を再生または再開する
                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playButton);

        // "一時停止"ボタン
        JButton pauseButton = new JButton(loadImage("src/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 一時停止ボタンをオフにして再生ボタンをオンにする
                enablePlayButtonDisablePauseButton();

                // 曲を一時停止する
                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseButton);

        // "次へ"ボタン
        JButton nextButton = new JButton(loadImage("src/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 次の曲へ進む
                musicPlayer.nextSong();
            }
        });
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    // music playerクラスからスライダーを更新するために使用
    public void setPlaybackSliderValue(int frame) {
        playbackSlider.setValue(frame);
    }

    public void updateSongTitleAndArtist(Song song) {
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    public void updatePlaybackSlider(Song song) {
        // スライダーの最大数を更新
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        // 曲の長さラベルを作成
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        // 00:00から開始
        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        // 曲によって終わり方が変わります
        JLabel labelEnd = new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
    }

    public void enablePauseButtonDisablePlayButton() {
        // playbackBtnsパネルからプレイバックボタンへの参照を取得
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);
        
        // 再生ボタンをオフにする
        playButton.setVisible(false);
        playButton.setEnabled(false);

        // 一時停止ボタンをオンにする
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }

    public void enablePlayButtonDisablePauseButton() {
        // playbackBtnsパネルからプレイバックボタンへの参照を取得
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);
        
        // 再生ボタンをオンにする
        playButton.setVisible(true);
        playButton.setEnabled(true);

        // 一時停止ボタンをオフにする
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private ImageIcon loadImage(String imagePath) {
        try {
            // 指定されたパスから画像を読み込む
            BufferedImage image = ImageIO.read(new File(imagePath));

            // コンポーネントが画像をレンダリングできるように、画像アイコンを返す
            return new ImageIcon(image);
        }catch(Exception e) {
            e.printStackTrace();
        }

        // リソースが見つからなかった場合
        return null;
    }
}

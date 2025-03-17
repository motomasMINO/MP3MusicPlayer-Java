import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MusicPlaylistDialog extends JDialog {
    private MusicPlayerGUI musicPlayerGUI;

    // プレイリストをロードするときに書き込まれるすべてのパスをtxtファイルに保存
    private ArrayList<String> songPaths;

    public MusicPlaylistDialog(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
        songPaths = new ArrayList<>();

        // ダイアログの設定
        setTitle("プレイリスト作成");
        setSize(400, 400);
        setResizable(false);
        getContentPane().setBackground(MusicPlayerGUI.FRAME_COLOR);
        setLayout(null);
        setModal(true); // フォーカスを与えるにはダイアログを閉じる必要があります
        setLocationRelativeTo(musicPlayerGUI);

        addDialogComponents();
    }

    private void addDialogComponents() {
        // 各曲のパスを保持するコンテナ
        JPanel songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int)(getWidth() * 0.025), 10, (int)(getWidth() * 0.90), (int) (getHeight() * 0.75));
        add(songContainer);

        // 曲追加ボタン
        JButton addSongButton = new JButton("追加");
        addSongButton.setBounds(60, (int) (getHeight() * 0.80), 100, 25);
        addSongButton.setFont(new Font("Dialog", Font.BOLD, 14));
        addSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ファイルエクスプローラーを開く
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));
                int result = jFileChooser.showOpenDialog(MusicPlaylistDialog.this);

                File selectedFile = jFileChooser.getSelectedFile();
                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    JLabel filePathLabel = new JLabel(selectedFile.getPath());
                    filePathLabel.setFont(new Font("Dialog", Font.BOLD, 12));
                    filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    // リストに追加
                    songPaths.add(filePathLabel.getText());

                    // コンテナに追加
                    songContainer.add(filePathLabel);

                    // ダイアログを更新して、新しく追加されたJLabelを表示
                    songContainer.revalidate();
                }
            }
        });
        add(addSongButton);

        // プレイリスト保存ボタン
        JButton savePlaylistButton = new JButton("保存");
        savePlaylistButton.setBounds(215, (int) (getHeight() * 0.80), 100, 25);
        savePlaylistButton.setFont(new Font("Dialog", Font.BOLD, 14));
        savePlaylistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    JFileChooser jFileChooser = new JFileChooser();
                    jFileChooser.setCurrentDirectory(new File("src/assets"));
                    int result = jFileChooser.showSaveDialog(MusicPlaylistDialog.this);
    
                    if(result == JFileChooser.APPROVE_OPTION) {
                        // getSelectedFile()を使用して、保存しようとしているファイルへの参照を取得
                        File selectedFile = jFileChooser.getSelectedFile();
    
                        // まだ変換していない場合は.txtファイルに変換
                        // ファイルに".txt"ファイル拡張子がついていないかどうか確認
                        if(!selectedFile.getName().substring(selectedFile.getName().length() - 4).equalsIgnoreCase(".txt")) {
                            selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                        }
    
                        // 目的のディレクトリに新しいファイルを作成
                        selectedFile.createNewFile();

                        // 曲のパスをすべてファイルに書き込む
                        FileWriter fileWriter = new FileWriter(selectedFile);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                        // 曲のパスのリストを繰り返し処理し、各文字列をファイルに書き込む
                        // 各曲はそれぞれの行に書き込まれる
                        for(String songPath : songPaths) {
                            bufferedWriter.write(songPath + "\n");
                        }
                        bufferedWriter.close();
                    
                        // 成功ダイアログを表示
                        JOptionPane.showMessageDialog(MusicPlaylistDialog.this, "プレイリストの作成に成功しました!");

                        // ダイアログを閉じる
                        MusicPlaylistDialog.this.dispose();
                    }
                }catch(Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        add(savePlaylistButton);
    }
}
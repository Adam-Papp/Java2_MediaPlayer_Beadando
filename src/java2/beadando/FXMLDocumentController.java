package java2.beadando;


import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;



public class FXMLDocumentController implements Initializable
{
    //  Zeneszámok táblázat a Könyvtár tabon
    ObservableList<Song> songs = FXCollections.observableArrayList();
    @FXML
    private TableView<Song> TableViewSongs = new TableView<Song>();
    @FXML
    private TableColumn<Song, String> nameColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, String> lengthColumn = new TableColumn<>();
    @FXML
    private TableColumn<Song, Integer> playCountColumn = new TableColumn<>();
    
    
    
    //  Lejátszás
    private static MediaPlayer mediaPlayer;
    private static Media media;
    private static String source;
    private boolean isPlaying;
    private int currentPlayingIdx;
    @FXML
    private Label currentPlaying;
    @FXML
    private Slider volumeSlider;
    @FXML
    private AreaChart spectrum;
    
    
    
    //  Playlist lista
    private ObservableList<playList> playLists = FXCollections.observableArrayList();
    
    
    
    //Lejatszasi lista oldali PlayList tablazat
    @FXML
    private TableView<playList> TableViewPlayList = new TableView<playList>();
    @FXML
    private TableColumn<playList, String> playListNameColumn = new TableColumn<>();
    @FXML
    private TableColumn<playList, String> playListSizeColumn = new TableColumn<>();
    @FXML
    private TextField playListField;
    
    
    
    //Fooldali tablazat
    @FXML
    private TableView<playList> TableViewPlayList2 = new TableView<playList>();
    @FXML
    private TableColumn<playList, String> playListNameColumn2 = new TableColumn<>();
    @FXML
    private TableColumn<playList, String> playListSizeColumn2 = new TableColumn<>();
    
    
    
    //Lejátszási oldali PlayList-ben lévő zenék táblázat
    @FXML
    private TableView<Song> TableViewPlayListSongs = new TableView<>();
    @FXML
    private TableColumn<Song, String> PlayListSongNameColumn = new TableColumn<>();
    
    
    
    //Lejátszási oldali összes zenék táblázat
    @FXML
    private TableView<Song> TableViewPlayListAllSongs = new TableView<>();
    @FXML
    private TableColumn<Song, String> playListAllSongsColumn = new TableColumn<>();
    
    
    
    //  Statisztikák
    @FXML
    private Label totalPlayingDuration;
    @FXML
    private Label playedFilesCount;
    private int playedFilesCountInt = 0;
    private String totalPlayingDurationString = "";
    private int totalPlayingDurationInteger = 0;
    private int totalMinutesCount = 0;
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        // Mappából file-ok kinyerése
        File[] songFiles = io.getFilesInFolder("songs");
        
        
        
        // Zene lista feltöltése 
        int duration = 0;
        int minutesCount = 0;
        String lengthStr = "";
        for (File f : songFiles)
        {
            try {
              AudioFile audioFile = AudioFileIO.read(new File("songs/" + f.getName()));
              duration = audioFile.getAudioHeader().getTrackLength();
            } catch (Exception e) {
              e.printStackTrace();
            }
            
            while (duration > 59)
            {
                minutesCount++;
                duration -= 60;
            }
            
            if (duration < 10)
            {
                lengthStr = minutesCount + ":0" + duration;
            }
            else
            {
                lengthStr = minutesCount + ":" + duration;
            }
            
            songs.add(new Song(f, f.getName().substring(0, f.getName().length()-4), lengthStr));
            
            lengthStr = "";
            duration = 0;
            minutesCount = 0;
        }
        
        
        
        //  Összes zenék playList
        playList osszesZenekPlayList = new playList("Összes zenék");
        osszesZenekPlayList.setSongs(songs);
        playLists.add(osszesZenekPlayList);
        
        
        
        //  Zenék kiírása konzolra
        for (Song s : songs)
        {
            System.out.println(s.toString());
        }
        
        
        
        //  Lejátszási lista oldalon összes zenék táblázat beállítása
        playListAllSongsColumn.setCellValueFactory(new PropertyValueFactory<>("songName"));
        TableViewPlayListAllSongs.setItems(songs);
        TableViewPlayListAllSongs.refresh();
        
        
        
        // Lejátszási lista oldali - Lejátszási lista tableview beállítása
        playListNameColumn.setCellValueFactory(new PropertyValueFactory<>("playListName"));
        playListSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        TableViewPlayList.setItems(playLists);
        TableViewPlayList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<playList>() {

            @Override
            public void changed(ObservableValue<? extends playList> observable, playList oldValue, playList newValue)
            {
                PlayListSongNameColumn.setCellValueFactory(new PropertyValueFactory<>("songName"));
                TableViewPlayListSongs.setItems(newValue.getSongs());
                TableViewPlayListSongs.refresh();
            }
        });
        
        
        
        // Fooldali Lejátszási lista tableview beállítása
        playListNameColumn2.setCellValueFactory(new PropertyValueFactory<>("playListName"));
        playListSizeColumn2.setCellValueFactory(new PropertyValueFactory<>("size"));
        TableViewPlayList2.setItems(playLists);
        TableViewPlayList2.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<playList>() {

            @Override
            public void changed(ObservableValue<? extends playList> observable, playList oldValue, playList newValue)
            {
                nameColumn.setCellValueFactory(new PropertyValueFactory<>("songName"));
                lengthColumn.setCellValueFactory(new PropertyValueFactory<>("songLength"));
                playCountColumn.setCellValueFactory(new PropertyValueFactory<>("playCount"));
                TableViewSongs.setItems(newValue.getSongs());
                TableViewSongs.refresh();
            }
        });
        
        
        
        // Real time tableview kiválasztásfigyelő
        TableViewSongs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                if (isPlaying)
                {
                    mediaPlayer.stop();
                }
                isPlaying = false;
                Song tempSong = (Song) observable.getValue();
                try
                {
                    playSong(tempSong);
                }catch(Exception e){
                }
            }
        });
    }   //  initialize vége
    
    
    
    public void playSong(Song tempSong)
    {
        //  MediaPlayer beállítása
        source = null;
        currentPlaying.setText(tempSong.getSongName());
        String tempSongName = tempSong.getSongName().concat(".mp3");
        source = new File("songs/" + tempSongName).toURI().toString();
        media = new Media(source);
        mediaPlayer = new MediaPlayer(media);
        
        
        
        //  Hangerőszabályzó beállítása
        mediaPlayer.setVolume(0.2);
        volumeSlider.setValue(mediaPlayer.getVolume() * 100);
        volumeSlider.valueProperty().addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            }
        });
        
        
        
        //  Zene lejátszása, lejátszási frekvencia növelése, statisztikák kiírása
        if (!isPlaying)
        {
            mediaPlayer.play();
            isPlaying = true;

            for (int i=0; i<songs.size(); i++)
            {
                if (songs.get(i) == tempSong)
                {
                    currentPlayingIdx = i;
                }
            }
            
            songs.get(currentPlayingIdx).setPlayCount(songs.get(currentPlayingIdx).getPlayCount()+1);
            TableViewSongs.refresh();
            
            
            
            playedFilesCountInt++;
            playedFilesCount.setText(String.valueOf(playedFilesCountInt));
            
            
            
            String songDuration[] = tempSong.getSongLength().split(":");
            totalPlayingDurationInteger = (Integer.parseInt(songDuration[0])*60) + Integer.parseInt(songDuration[1]);
            
            int tempTotalPlayingDurationInteger = 0;
            
            if (playedFilesCountInt > 1)
            {
                String tempSongduration[] = totalPlayingDuration.getText().split(":");
                tempTotalPlayingDurationInteger = (Integer.parseInt(tempSongduration[0])*60) + Integer.parseInt(tempSongduration[1]);
            }
            
            totalPlayingDurationInteger += tempTotalPlayingDurationInteger;
            
            while (totalPlayingDurationInteger > 59)
            {
                totalMinutesCount++;
                totalPlayingDurationInteger -= 60;
            }
            
            if (totalPlayingDurationInteger < 10)
            {
                totalPlayingDurationString = totalMinutesCount + ":0" + totalPlayingDurationInteger;
            }
            else
            {
                totalPlayingDurationString = totalMinutesCount + ":" + totalPlayingDurationInteger;
            }
            
            totalPlayingDuration.setText(totalPlayingDurationString);
            totalMinutesCount = 0;
        }
        
        
        
        //  Audio Spectrum beállítása
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        int BANDS = mediaPlayer.getAudioSpectrumNumBands();
        XYChart.Data[] series1Data = new XYChart.Data[BANDS];  

        for (int i = 0; i < series1Data.length; i++)
        {  
            series1Data[i] = new XYChart.Data<>(Integer.toString(i + 1), 0);  
            series1.getData().add(series1Data[i]);  
        }

        spectrum.getData().add(series1);
        
        mediaPlayer.setAudioSpectrumListener(new AudioSpectrumListener() {
            
            float[] buffer = createFilledBuffer(BANDS, mediaPlayer.getAudioSpectrumThreshold());

            @Override
            public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases)
            {
                for (int i = 0; i < magnitudes.length; i++) {  
                    
                    if (magnitudes[i] >= buffer[i])
                    {  
                        buffer[i] = magnitudes[i]; 
                        series1Data[i].setYValue(magnitudes[i] - mediaPlayer.getAudioSpectrumThreshold());  
                    }
                    else
                    {
                        series1Data[i].setYValue(buffer[i] - mediaPlayer.getAudioSpectrumThreshold());  
                        buffer[i] -= 0.25;  
                    }
                }
            }
        });
    }   //  playSong vége
    
    
    
    //  Lejátszás gomb onClick
    public void playButton(MouseEvent event)
    {
        if (!isPlaying)
            mediaPlayer.play();
        isPlaying = true;
    }
    
    
    
    //  Szünet gomb onClick
    public void pauseButton(MouseEvent event)
    {
        mediaPlayer.pause();
        isPlaying = false;
    }
    
    
    
    //  Megállítás gomb onClick
    public void stopButton(MouseEvent event)
    {
        mediaPlayer.stop();
        isPlaying = false;
    }
    
    
    
    //  Előző gomb onClick
    public void previousButton(MouseEvent event)
    {
        mediaPlayer.stop();
        isPlaying = false;
        currentPlayingIdx--;
        if (songs.get(currentPlayingIdx).getSongName().contains(".mp3"))
            songs.get(currentPlayingIdx).setSongName(songs.get(currentPlayingIdx).getSongName().substring(0, songs.get(currentPlayingIdx).getSongName().length()-4));
        playSong(songs.get(currentPlayingIdx));
    }
    
    
    
    //  Következő gomb onClick
    public void nextButton(MouseEvent event)
    {
        mediaPlayer.stop();
        isPlaying = false;
        currentPlayingIdx++;
        if (songs.get(currentPlayingIdx).getSongName().contains(".mp3"))
            songs.get(currentPlayingIdx).setSongName(songs.get(currentPlayingIdx).getSongName().substring(0, songs.get(currentPlayingIdx).getSongName().length()-4));
        playSong(songs.get(currentPlayingIdx));
    }
    
    
    
    //  Audio Spectrum metódusa
    private float[] createFilledBuffer(int size, float fillValue) {  
        float[] floats = new float[size];  
        Arrays.fill(floats, fillValue);  
        return floats;  
    }
    
    
    
    //  Új lista hozzáadás onClick
    public void addNewList(MouseEvent event)
    {
        if(!playListField.getText().equals(""))
        {
            playLists.add(new playList(playListField.getText()));
            TableViewPlayList.setItems(playLists);
            TableViewPlayList2.setItems(playLists);
//              playListView.getItems().add(playListField.getText());
            playListField.clear();
        }  
    }
    
    
    
    //  PlayList törlés gomb onClick
    public void deletePlayList(MouseEvent event)
    {
        if (TableViewPlayList.getSelectionModel().getSelectedItem().getPlayListName() != "Összes zenék")
            playLists.remove(TableViewPlayList.getSelectionModel().getSelectedItem());
    }
    
    
    
    //  PlayListhez hozzáad gomb onClick
    public void AddSongToPlayList(MouseEvent event)
    {
        Song tempSong = TableViewPlayListAllSongs.getSelectionModel().getSelectedItem();
        playList tempPlayList = TableViewPlayList.getSelectionModel().getSelectedItem();
        ObservableList<Song> tempPlayListSongs = FXCollections.observableArrayList();
        tempPlayListSongs = tempPlayList.getSongs();
        boolean songExistsInPlayList = false;
        
        for (Song s : tempPlayListSongs)
        {
            if (s.getSongName() == tempSong.getSongName())
                songExistsInPlayList = true;
        }
        
        if (songExistsInPlayList == false)
            tempPlayList.addSong(tempSong);
        
        TableViewPlayListSongs.refresh();
        TableViewPlayList.refresh();
        TableViewPlayList2.refresh();
    }
    
    
    
    //  PlayListből törlés gomb onClick
    public void DeleteSongFromPlayList(MouseEvent event)
    {
        Song tempSong = TableViewPlayListSongs.getSelectionModel().getSelectedItem();
        playList tempPlayList = TableViewPlayList.getSelectionModel().getSelectedItem();
        
        tempPlayList.removeSong(tempSong);
        
        TableViewPlayListSongs.refresh();
        TableViewPlayList.refresh();
        TableViewPlayList2.refresh();
    }
    
}   //  FXMLDocumentController osztály vége
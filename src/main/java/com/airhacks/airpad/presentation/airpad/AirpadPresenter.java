package com.airhacks.airpad.presentation.airpad;

import com.airhacks.airpad.business.notes.boundary.NotesStore;
import com.airhacks.airpad.business.notes.entity.Note;
import com.airhacks.airpad.presentation.notelist.NoteListPresenter;
import com.airhacks.airpad.presentation.notelist.NoteListView;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javax.inject.Inject;

/**
 *
 * @author adam-bien.com
 */
public class AirpadPresenter implements Initializable {
    
    @FXML
    TextField noteName;
    @FXML
    TextArea noteContent;
    @Inject
    NotesStore store;
    @FXML
    AnchorPane noteList;
    private NoteListView noteListView;
    private NoteListPresenter noteListPresenter;
    private ObjectProperty<Note> selectedNote;
    private StringProperty title;
    private ObservableList<Note> filteredNotes;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.initModel();
        this.selectedNote = new SimpleObjectProperty<>();
        this.noteListView = new NoteListView();
        this.noteListPresenter = (NoteListPresenter) this.noteListView.getPresenter();
        this.noteListPresenter.bind(filteredNotes());
        Parent view = this.noteListView.getView();
        this.noteList.getChildren().add(view);
        title().bind(this.noteName.textProperty());
        this.selectedNote.bind(this.noteListPresenter.selectedNote());
        this.selectedNote.addListener(new ChangeListener<Note>() {
            @Override
            public void changed(ObservableValue<? extends Note> ov, Note old, Note newNote) {
                if (newNote == null) {
                    return;
                }
                noteContent.setText(newNote.contentProperty().get());
            }
        });
        noteContent.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String old, String newValue) {
                Note note = selectedNote.getValue();
                if (note != null) {
                    note.contentProperty().set(newValue);
                    store.update(note);
                }
            }
        });
        this.noteListPresenter.nodeSelected().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean old, Boolean newValue) {
                noteContent.requestFocus();
            }
        });
        this.registerListeners();
    }
    
    void registerListeners() {
        this.store.addedProperty().addListener(new ChangeListener<Note>() {
            @Override
            public void changed(ObservableValue<? extends Note> ov, Note old, Note newNote) {
                System.out.println("Added: " + newNote);
                filteredNotes.add(newNote);
            }
        });
        
        this.store.removedProperty().addListener(new ChangeListener<Note>() {
            @Override
            public void changed(ObservableValue<? extends Note> ov, Note old, Note newNote) {
                System.out.println("Removed: " + newNote);
                filteredNotes.remove(newNote);
            }
        });
        
        this.store.updatedProperty().addListener(new ChangeListener<Note>() {
            @Override
            public void changed(ObservableValue<? extends Note> ov, Note old, Note newNote) {
                for (int i = 0; i < filteredNotes.size(); i++) {
                    Note note = filteredNotes.get(i);
                    if (note.equals(newNote)) {
                        note.from(newNote);
                    }
                }
            }
        });
    }
    
    void initModel() {
        this.filteredNotes = FXCollections.observableArrayList();
        this.title = new SimpleStringProperty();
        this.title.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {
                filteredNotes.clear();
                for (Note note : store.allNotes()) {
                    if (note.matches(newValue)) {
                        filteredNotes.add(note);
                    }
                }
            }
        });
        
    }
    
    public void noteNameEntered() {
        create(noteName.getText());
    }
    
    public void create(String text) {
        final Note note = new Note(text);
        this.store.create(note);
    }
    
    public ObservableList<Note> filteredNotes() {
        return this.filteredNotes;
    }
    
    public StringProperty title() {
        return this.title;
    }
    
    public void save() {
        this.store.save();
    }
}

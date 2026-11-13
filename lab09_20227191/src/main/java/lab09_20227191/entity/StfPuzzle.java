package lab09_20227191.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stf_puzzle_sv")
public class StfPuzzle {
    @Id
    @Column(name = "stf_game_board_structure")
    private String stfGameBoardStructure;

    // Imagen como arreglo de bytes (clase 5.3, metodo 2: BLOB)
    @Lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;

    public StfPuzzle() {
    }

    public String getStfGameBoardStructure() { return stfGameBoardStructure; }
    public void setStfGameBoardStructure(String stfGameBoardStructure) { this.stfGameBoardStructure = stfGameBoardStructure; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}

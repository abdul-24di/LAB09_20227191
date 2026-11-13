package lab09_20227191.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stf_puzzle_sv")
public class StfPuzzle {
    @Id
    @Column(name = "stf_game_board_structure")
    private String stfBoardStruct;

    @Lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;

    public StfPuzzle() {

    }

    public String getStfBoardStruct() {
        return stfBoardStruct;
    }

    public void setStfBoardStruct(String stfBoardStruct) {
        this.stfBoardStruct = stfBoardStruct;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}

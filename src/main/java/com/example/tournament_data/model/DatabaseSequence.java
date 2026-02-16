package com.example.tournament_data.model;  
  
import org.springframework.data.annotation.Id;  
import org.springframework.data.mongodb.core.mapping.Document;  
  
import lombok.AllArgsConstructor;  
import lombok.Data;  
import lombok.NoArgsConstructor;  
  
@Document(collection = "sequences")  
@Data  
@NoArgsConstructor  
@AllArgsConstructor  
public class DatabaseSequence {  
      
    @Id  
    private String id; // This will be the sequence name like "players_sequence", "teams_sequence"  
      
    private Integer seq; // Current sequence value  
}  

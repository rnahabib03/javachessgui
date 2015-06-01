package javachessgui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import javafx.stage.*;

import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class Game {
    
        String initial_dir="";
    
        ListView<String> list = new ListView<String>();
        
        TextArea pgn_text=new TextArea();
    
        public VBox vertical_box=new VBox(2);
        FileChooser f=new FileChooser();
        
        private Stage s=new Stage();
        
        private Board b;
        
        private TextArea game_text = new TextArea ();
        
        final private int MAX_MOVES=250;
        
        String[] pgn_lines=new String[MAX_MOVES];
        
        private String[] moves=new String[MAX_MOVES];
        public String initial_position;
        private String[] positions=new String[MAX_MOVES];
        
        private int move_ptr=0;
        
        private int game_ptr=0;
        
        public void reset(String initial_fen)
        {
            
            move_ptr=0;
            
            initial_position=initial_fen;
            
            update_game();
            
        }
        
        public void add_move(String san,String fen_after)
        {
            
            if(game_ptr<move_ptr)
            {
                game_ptr=move_ptr;
            }
            
            if(move_ptr>=MAX_MOVES)
            {
                
            }
            else
            {
                
                positions[move_ptr]=fen_after;
                moves[move_ptr++]=san;
                game_ptr++;
                
            }
            
            update_game();
            
        }
        
        public String to_begin()
        {
            game_ptr=0;
            update_game();
            return initial_position;
        }
        
        public String back()
        {
            
            if(game_ptr==0)
            {
                return initial_position;
            }
            else
            {
                game_ptr--;
                update_game();
                if(game_ptr==0)
                {
                    return initial_position;
                }
                return positions[game_ptr-1];
                
            }
            
        }
        
        public String forward()
        {
            
            if(game_ptr==move_ptr)
            {
                if(game_ptr==0)
                {
                    return initial_position;
                }
                return positions[game_ptr-1];
            }
            else
            {
                game_ptr++;
                update_game();
                return positions[game_ptr-1];
                
            }
            
        }
        
        public String to_end()
        {
            game_ptr=move_ptr;
            update_game();
            if(move_ptr==0)
            {
                return initial_position;
            }
            return positions[move_ptr-1];
        }
        
        public String delete_move()
        {
            
            if(game_ptr<move_ptr)
            {
                move_ptr=game_ptr;
            }
            
            if(move_ptr==0)
            {
                // nothing to delete
                return(initial_position);
            }
            else
            {
                if(move_ptr>0)
                {
                    move_ptr--;
                    game_ptr--;
                }
                
                update_game();
                
                if(move_ptr==0)
                {
                    return initial_position;
                }
                
                return(positions[move_ptr-1]);
            }
        }
        
        private void update_game()
        {
            
            String[] game_buffer=new String[MAX_MOVES+1];
            
            game_buffer[0]="*";
            
            for(int i=0;i<move_ptr;i++)
            {
                game_buffer[i+1]=moves[i];
            }

            //game_text.setText(game_buffer);
            
            ObservableList<String> items =FXCollections.observableArrayList(
                Arrays.copyOfRange(game_buffer, 0, move_ptr+1)
            );
        
            list.setItems(items);
            
            list.getSelectionModel().select(game_ptr);
            list.scrollTo(game_ptr);
            
            pgn_text.setText(calc_pgn());
                        
        }
        
        private String pgn;
        public String calc_pgn()
        {
            Board dummy=new Board(false);
            
            dummy.set_from_fen(initial_position);
            
            int fullmove_number=dummy.fullmove_number;
            int turn=dummy.turn;
            
            pgn="[StartFen \""+initial_position+"\"]";
            
            pgn+="\n\n";
            
            if(move_ptr>0)
            {
                pgn+=fullmove_number+". ";
                
                if(turn==Board.TURN_BLACK)
                {
                    pgn+="... ";
                }
                
                pgn+=moves[0]+" ";
            }
            
            for(int i=1;i<move_ptr;i++)
            {
                dummy.set_from_fen(positions[i-1]);
                turn=dummy.turn;
                if(dummy.turn==Board.TURN_WHITE)
                {
                    fullmove_number++;
                    pgn+=fullmove_number+". ";
                }
                pgn+=moves[i]+" ";
            }
            
            return pgn;
            
        }
        
        private void set_from_pgn_lines()
        {
            
            move_ptr=0;
            
            initial_position="rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            
            int line_cnt=0;
            
            // read headers
            int empty_cnt=0;
            
            Boolean finished=false;
            
            do
            {
                String line=pgn_lines[line_cnt++];
                
                if(line_cnt<pgn_lines.length)
                {
                    if(line.length()<2)
                    {
                        finished=true;
                    }
                    else
                    {
                        if(line.charAt(0)!='[')
                        {
                            finished=true;
                        }
                        else
                        {
                            //System.out.println("header "+line);
                        }
                    }
                }
                else
                {
                    finished=true;
                }
                
            }while(!finished);
            
            String body="";
            while(line_cnt<pgn_lines.length)
            {
                String line=pgn_lines[line_cnt++];
                if(line.length()<2)
                {
                    break;
                }
                body+=line+" ";
            }
            
            System.out.println("body: "+body);
            
            MyTokenizer t=new MyTokenizer(body);
            
            String token;
            
            b.reset();
            
            while((token=t.get_token())!=null)
            {
                System.out.println("token: "+token);
                
                if(b.is_san_move_legal(token))
                {
                    b.make_san_move(token, false);
                    String fen_after=b.report_fen();
                    add_move(token,fen_after);
                    
                    System.out.println("san: "+token+" "+fen_after);
                }
                
            }
            
            b.drawBoard();
            
        }
        
        public Game(Stage set_s,Board set_b)
        {
            
            s=set_s;
            b=set_b;
            
            Button open_pgn_button=new Button();
            open_pgn_button.setText("Open PGN");
            
            open_pgn_button.setOnAction(new EventHandler<ActionEvent>() {
                    
                @Override public void handle(ActionEvent e) {
                    
                    if(initial_dir!="")
                    {
                        File dir=new File(initial_dir);

                        f.setInitialDirectory(dir);
                    }
                                        
                     File file = f.showOpenDialog(s);
                     
                     String path=file.getPath();
                     
                     initial_dir=path.substring(0,path.lastIndexOf(File.separator));
                     
                     MyFile my_file=new MyFile(path);
                     
                     pgn_lines=my_file.read_lines();
                     
                     set_from_pgn_lines();
                     
                    }
                
            });
            
            vertical_box.getChildren().add(open_pgn_button);
            
            list.setMaxWidth(120);
            
            vertical_box.getChildren().add(list);
            
            vertical_box.getChildren().add(pgn_text);
            
            list.setOnMouseClicked(new EventHandler<Event>() {

                        @Override
                        public void handle(Event event) {
                            
                            int selected =  list.getSelectionModel().getSelectedIndex();

                            String pos=initial_position;
                            if(selected>0){pos=positions[selected-1];}
                            
                            game_ptr=selected;
                            
                            b.set_from_fen_inner(pos,false);
                            b.drawBoard();

                    }

                });
            
            
        }
    
}

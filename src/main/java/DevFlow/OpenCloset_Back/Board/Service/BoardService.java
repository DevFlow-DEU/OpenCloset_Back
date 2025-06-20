package DevFlow.OpenCloset_Back.Board.Service;
import DevFlow.OpenCloset_Back.Board.Repository.*;
import DevFlow.OpenCloset_Back.Board.dto.req.BoardCreateRequestDto;
import DevFlow.OpenCloset_Back.Board.dto.res.*;
import DevFlow.OpenCloset_Back.Board.entity.*;
import DevFlow.OpenCloset_Back.Board.Repository.BoardRepository;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final TopRepository topRepository;
    private final BottomRepository bottomRepository;
    private final OuterRepository outerRepository;
    private final JewelryRepossitory jewelryRepossitory;
    private final One_pieceRepository onePieceRepository;
    private final ShoesRepository shoesRepository;


    @Transactional(readOnly = true)
        public List<BoardCreateResponsetDto> getPosts () {
            return boardRepository.findAllByOrderByModifiedAtDesc().stream().map(BoardCreateResponsetDto::new).toList();
        }

        @Transactional(readOnly = true)
        public List<TopsResponseDto> getTops() {
        return topRepository.findAll().stream()
                .map(TopsResponseDto::new)
                .toList();
        }

        @Transactional(readOnly = true)
        public List<BottomsReponseDto> getBottoms() {
            return bottomRepository.findAll().stream()
                    .map(BottomsReponseDto::new)
                    .toList();
        }

        @Transactional(readOnly = true)
        public List<OutherResponseDto> getOuters() {
            return outerRepository.findAll().stream()
                    .map(OutherResponseDto::new)
                    .toList();
        }
        @Transactional(readOnly = true)
        public List<One_pieceResponseDto> getOnePieces() {
            return onePieceRepository.findAll().stream()
                    .map(One_pieceResponseDto::new)
                    .toList();
        }
        @Transactional(readOnly = true)
        public List<JewelryResponseDto> getJewelry() {
            return jewelryRepossitory.findAll().stream()
                    .map(JewelryResponseDto::new)
                    .toList();
        }
        @Transactional(readOnly = true)
        public List<ShoesResponseDto> getShoes() {
            return shoesRepository.findAll().stream()
                    .map(ShoesResponseDto::new)
                    .toList();
        }

        @Transactional
        public BoardCreateResponsetDto createBoard (BoardCreateRequestDto req,User user){
            Board board = new Board(req,user);
            boardRepository.save(board);


            if (req.getCategory().equals("top")) {   //상의
                Top top = new Top(board);
                topRepository.save(top);
            }
            if (req.getCategory().equals("bottom")) {    //하의
                Bottom bottom = new Bottom(board);
                bottomRepository.save(bottom);
            }
            if (req.getCategory().equals("outer")) {     //아우터
                Outer_ outer = new Outer_(board);
                outerRepository.save(outer);
            }
            if (req.getCategory().equals("jewelry")) {   //주얼리
                Jewelry jewelry = new Jewelry(board);
                jewelryRepossitory.save(jewelry);
            }
            if (req.getCategory().equals("one piece")) {   //원피스
                One_Piece one_piece = new One_Piece(board);
                onePieceRepository.save(one_piece);
            }
            if (req.getCategory().equals("shoes")) {
                Shoes shoes = new Shoes(board);
                shoesRepository.save(shoes);
            }

            return new BoardCreateResponsetDto(board);
        }
    @Transactional(readOnly = true)
    public List<BoardCreateResponsetDto> getPostsByAddress(String address) {
        return boardRepository.findByUser_AddressOrderByModifiedAtDesc(address)
                .stream()
                .map(BoardCreateResponsetDto::new)
                .toList();
    }
        @Transactional
        public BoardCreateResponsetDto getPost (Long id){
            return boardRepository.findById(id).map(BoardCreateResponsetDto::new).orElseThrow(
                    () -> new IllegalArgumentException("아이디가 존재하지않습니다.")
            );
        }



    }


package DevFlow.OpenCloset_Back.Board.Service;

import DevFlow.OpenCloset_Back.Board.Repository.*;
import DevFlow.OpenCloset_Back.Board.dto.req.*;
import DevFlow.OpenCloset_Back.Board.dto.res.*;
import DevFlow.OpenCloset_Back.Board.entity.*;
import DevFlow.OpenCloset_Back.Board.Repository.BoardRepository;
import DevFlow.OpenCloset_Back.Board.entity.Board;
import DevFlow.OpenCloset_Back.User.User_Repository.UserRepository;
import DevFlow.OpenCloset_Back.User.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private final BagRepository bagRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BoardCreateResponsetDto> getPosts() {
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

    @Transactional(readOnly = true)
    public List<BagsResponseDto> getBags() {
        return bagRepository.findAll().stream()
                .map(BagsResponseDto::new)
                .toList();
    }

    @Transactional
    public BoardCreateResponsetDto createBoard(BoardCreateRequestDto req, User seller) throws IOException {

        List<MultipartFile> files = req.getImages();
        List<String> imagePaths = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            String uploadDir = "uploads/boards/";
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
                    Path filePath = uploadPath.resolve(uniqueFilename);
                    file.transferTo(filePath.toFile());
                    imagePaths.add("/" + uploadDir + uniqueFilename);
                }
            }
        }

        Board board = new Board(req, imagePaths, seller);
        boardRepository.save(board);

        if (req.getCategory().equals("top")) { // 상의
            Top top = new Top(board);
            topRepository.save(top);
        }
        if (req.getCategory().equals("bottom")) { // 하의
            Bottom bottom = new Bottom(board);
            bottomRepository.save(bottom);
        }
        if (req.getCategory().equals("outer")) { // 아우터
            Outer_ outer = new Outer_(board);
            outerRepository.save(outer);
        }
        if (req.getCategory().equals("jewelry")) { // 주얼리
            Jewelry jewelry = new Jewelry(board);
            jewelryRepossitory.save(jewelry);
        }
        if (req.getCategory().equals("one piece")) { // 원피스
            One_Piece one_piece = new One_Piece(board);
            onePieceRepository.save(one_piece);
        }
        if (req.getCategory().equals("shoes")) {
            Shoes shoes = new Shoes(board);
            shoesRepository.save(shoes);
        }
        if (req.getCategory().equals("bag")) {
            Bag bag = new Bag(board);
            bagRepository.save(bag);
        }

        return new BoardCreateResponsetDto(board);
    }

    @Transactional(readOnly = true)
    public List<BoardCreateResponsetDto> getPostsByAddress(String address) {
        return boardRepository.findBySeller_AddressOrderByModifiedAtDesc(address)
                .stream()
                .map(BoardCreateResponsetDto::new)
                .toList();
    }

    @Transactional
    public BoardCreateResponsetDto getPost(Long id) {
        return boardRepository.findById(id).map(BoardCreateResponsetDto::new).orElseThrow(
                () -> new IllegalArgumentException("아이디가 존재하지않습니다."));
    }

    // ============================================
    // 상태관리 로직
    // ============================================

    /**
     * 게시물 상태 변경 (한 방향, 4단계)
     * 대여가능 → 예약중 (buyerId 필수) → 대여중 → 대여완료
     * 본인(seller)만 변경 가능
     */
    @Transactional
    public BoardCreateResponsetDto updateStatus(Long boardId, String status, User user, Long buyerId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다. id=" + boardId));

        // 본인 게시물인지 확인
        if (!board.getSeller().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 게시물만 상태를 변경할 수 있습니다.");
        }

        String currentStatus = board.getStatus();

        // 상태 전환 검증: 대여가능 → 예약중 → 대여중 → 대여완료 (한 방향만 가능)
        if (currentStatus.equals("대여가능") && status.equals("예약중")) {
            // 대여가능 → 예약중: buyerId 필수 (예약 단계에서 buyer 지정)
            if (buyerId == null) {
                throw new IllegalArgumentException("'예약중' 상태로 변경하려면 buyerId를 입력해야 합니다.");
            }
            User buyer = userRepository.findById(buyerId)
                    .orElseThrow(() -> new IllegalArgumentException("구매자(buyer)를 찾을 수 없습니다. id=" + buyerId));
            if (buyer.getId().equals(user.getId())) {
                throw new IllegalArgumentException("본인의 상품을 본인이 대여할 수 없습니다.");
            }
            board.setBuyer(buyer);

        } else if (currentStatus.equals("예약중") && status.equals("대여중")) {
            // 예약중 → 대여중: 실제로 옷을 건넸을 때 (buyer 유지)

        } else if (currentStatus.equals("대여중") && status.equals("대여완료")) {
            // 대여중 → 대여완료: 반납 완료 (buyer 유지 — 이력 보존)

        } else {
            throw new IllegalArgumentException(
                    "상태 전환이 불가능합니다. (대여가능→예약중→대여중→대여완료 순서만 가능) "
                    + "현재: " + currentStatus + " → 요청: " + status);
        }

        board.setStatus(status);
        boardRepository.save(board);

        return new BoardCreateResponsetDto(board);
    }

    /**
     * 내 게시물 상태별 필터 조회
     * status가 null이면 전체 조회
     */
    @Transactional(readOnly = true)
    public List<BoardCreateResponsetDto> getMyBoards(User user, String status) {
        List<Board> boards;

        if (status != null && !status.trim().isEmpty()) {
            boards = boardRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(user.getId(), status);
        } else {
            boards = boardRepository.findBySellerIdOrderByCreatedAtDesc(user.getId());
        }

        return boards.stream()
                .map(BoardCreateResponsetDto::new)
                .toList();
    }

    // ============================================
    // Owner / Renter 필터 로직
    // ============================================

    /**
     * Owner(빌려준) 상품 조회
     * 로그인 유저가 seller이면서 buyer가 존재하는 게시물 (실제로 빌려준 이력)
     * status가 null이면 전체 조회
     */
    @Transactional(readOnly = true)
    public List<BoardCreateResponsetDto> getOwnerBoards(User user, String status) {
        List<Board> boards;

        if (status != null && !status.trim().isEmpty()) {
            boards = boardRepository.findBySellerIdAndBuyerIsNotNullAndStatusOrderByCreatedAtDesc(user.getId(), status);
        } else {
            boards = boardRepository.findBySellerIdAndBuyerIsNotNullOrderByCreatedAtDesc(user.getId());
        }

        return boards.stream()
                .map(BoardCreateResponsetDto::new)
                .toList();
    }

    /**
     * Renter(빌린) 상품 조회
     * 로그인 유저가 buyer인 게시물
     * status가 null이면 전체 조회
     */
    @Transactional(readOnly = true)
    public List<BoardCreateResponsetDto> getRenterBoards(User user, String status) {
        List<Board> boards;

        if (status != null && !status.trim().isEmpty()) {
            boards = boardRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(user.getId(), status);
        } else {
            boards = boardRepository.findByBuyerIdOrderByCreatedAtDesc(user.getId());
        }

        return boards.stream()
                .map(BoardCreateResponsetDto::new)
                .toList();
    }

    /**
     * 게시물 수정 (내용, 카테고리, 이미지 등)
     * 본인(seller)만 수정 가능
     */
    @Transactional
    public BoardCreateResponsetDto updateBoard(Long boardId, BoardUpdateRequestDto req, User user) throws IOException {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다. id=" + boardId));

        // 본인 게시물인지 확인
        if (!board.getSeller().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 게시물만 수정할 수 있습니다.");
        }

        // 1. 기본 필드 업데이트 (null 또는 빈 값이 아닌 경우에만 부분 수정)
        if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) {
            board.setTitle(req.getTitle());
        }
        if (req.getDescription() != null && !req.getDescription().trim().isEmpty()) {
            board.setDescription(req.getDescription());
        }
        if (req.getSize() != null && !req.getSize().trim().isEmpty()) {
            board.setSize(req.getSize());
        }
        if (req.getSex() != null && !req.getSex().trim().isEmpty()) {
            board.setSex(req.getSex());
        }
        if (req.getLatitude() != null) {
            board.setLatitude(req.getLatitude());
        }
        if (req.getLongitude() != null) {
            board.setLongitude(req.getLongitude());
        }
        if (req.getPrice() != null) {
            board.setPrice(req.getPrice());
        }
        if (req.getStartDate() != null) {
            board.setStartDate(req.getStartDate());
        }
        if (req.getEndDate() != null) {
            board.setEndDate(req.getEndDate());
        }

        // 2. 이미지 업데이트 (신규 이미지가 들어왔을 때만 교체)
        List<MultipartFile> files = req.getImages();
        if (files != null && !files.isEmpty()) {
            List<String> imagePaths = new ArrayList<>();
            String uploadDir = "uploads/boards/";
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
                    Path filePath = uploadPath.resolve(uniqueFilename);
                    file.transferTo(filePath.toFile());
                    imagePaths.add("/" + uploadDir + uniqueFilename);
                }
            }

            if (!imagePaths.isEmpty()) {
                board.setImages(imagePaths);
            }
        }

        // 3. 카테고리 변경 시 매핑 테이블 업데이트
        String oldCategory = board.getCategory();
        String newCategory = req.getCategory();
        if (newCategory != null && !newCategory.trim().isEmpty() && !newCategory.equals(oldCategory)) {
            deleteCategoryMapping(board);
            board.setCategory(newCategory);
            saveCategoryMapping(board, newCategory);
        }

        boardRepository.save(board);
        return new BoardCreateResponsetDto(board);
    }

    private void deleteCategoryMapping(Board board) {
        String category = board.getCategory();
        if (category == null) return;

        List<Board> boards = List.of(board);
        switch (category) {
            case "top" -> topRepository.deleteByBoardIn(boards);
            case "bottom" -> bottomRepository.deleteByBoardIn(boards);
            case "outer" -> outerRepository.deleteByBoardIn(boards);
            case "jewelry" -> jewelryRepossitory.deleteByBoardIn(boards);
            case "one piece" -> onePieceRepository.deleteByBoardIn(boards);
            case "shoes" -> shoesRepository.deleteByBoardIn(boards);
            case "bag" -> bagRepository.deleteByBoardIn(boards);
        }
    }

    private void saveCategoryMapping(Board board, String category) {
        switch (category) {
            case "top" -> topRepository.save(new Top(board));
            case "bottom" -> bottomRepository.save(new Bottom(board));
            case "outer" -> outerRepository.save(new Outer_(board));
            case "jewelry" -> jewelryRepossitory.save(new Jewelry(board));
            case "one piece" -> onePieceRepository.save(new One_Piece(board));
            case "shoes" -> shoesRepository.save(new Shoes(board));
            case "bag" -> bagRepository.save(new Bag(board));
        }
    }
}

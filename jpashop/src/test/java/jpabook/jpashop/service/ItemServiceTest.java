package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.Movie;
import jpabook.jpashop.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Slf4j
class ItemServiceTest {

    @Autowired
    ItemService itemService;
    @Autowired
    ItemRepository itemRepository;

    @Test
    @Rollback(value = false)
    void saveItem() {
        // given
        Movie movie = new Movie();
        movie.setName("타이타닉");

        // when
        itemService.saveItem(movie);
        log.info("movie id = {}", movie.getId());

        // then
        assertThat(movie).isEqualTo(itemRepository.findOne(movie.getId()));
    }

    @Test
    void findItems() {
        // given
        Movie movie = new Movie();
        movie.setName("타이타닉");
        Book book = new Book();
        book.setName("해리포터");
        Album album = new Album();
        album.setName("IU");
        itemRepository.save(movie);
        itemRepository.save(album);
        itemRepository.save(book);
        // when
        List<Item> items = itemService.findItems();

        // then
        assertThat(items).contains(movie, book, album);
    }

    @Test
    void findOne() {
        // given
        Movie movie = new Movie();
        movie.setName("올드보이");
        itemRepository.save(movie);

        // when
        Item findItem = itemService.findOne(movie.getId());

        // then
        assertThat(findItem.getId()).isEqualTo(movie.getId());
    }
}
package com.project_sem4.fe.controller;

import com.project_sem4.fe.entity.Category;
import com.project_sem4.fe.entity.Chapter;
import com.project_sem4.fe.entity.Story;
import com.project_sem4.fe.reponsitory.StoryRepository;
import com.project_sem4.fe.service.CategoryService;
import com.project_sem4.fe.service.ChapterService;
import com.project_sem4.fe.service.StoryService;
import com.project_sem4.fe.specification.SearchCriteria;
import com.project_sem4.fe.specification.StorySpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping(value = "/story")
public class StoryController {
    @Autowired
    StoryService storyService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ChapterService chapterService;

    @Autowired
    StoryRepository storyRepository;


    @RequestMapping(method = RequestMethod.GET, value = "/list")
    public String getAll( @RequestParam(name = "categoryId", required = false) Long categoryId,
                          @RequestParam(value = "keyword", required = false) String keyword,
                          @RequestParam(name = "page", defaultValue = "1") int page,
                          @RequestParam(name = "limit", defaultValue = "5") int limit,
                          @RequestParam(name = "story_id", required = false, defaultValue = "") Long story_id,
                          Model model) {
//        model.addAttribute("listStory", storyService.getAll());
        // khoi tao specification khi all param null
        Specification specification = Specification.where(null);
        if (categoryId != null && categoryId > 0) {
            specification = specification
                    .and(new StorySpecification(new SearchCriteria("categoryId", "joinCategory", categoryId)));
            model.addAttribute("categoryId", categoryId);
        }
        if (keyword != null && keyword.length() > 0) {
            specification = specification
                    .and(new StorySpecification(new SearchCriteria("keyword", "join", keyword)));
            model.addAttribute("keyword", keyword);
        }
        Page<Story> storyPage = storyService.findAllWithSpe(specification, PageRequest.of(page - 1, limit));

        model.addAttribute("keyword", keyword);
        model.addAttribute("listStory", storyPage.getContent());
        model.addAttribute("currentPage", storyPage.getPageable().getPageNumber() + 1);
        model.addAttribute("limit", storyPage.getPageable().getPageSize());
        model.addAttribute("totalPage", storyPage.getTotalPages());
        model.addAttribute("categories", categoryService.getAllCategory());
        // tung
//        Story story = storyService.getDetail(story_id);
//        Set<Category> categorySet = story.getCategories();
//        model.addAttribute("story", story);
//        model.addAttribute("categorySet", categorySet);

        return "story/list";
    }

    @RequestMapping(method = RequestMethod.GET)
    public String showList(Model model) {
        model.addAttribute("pageStory", storyService.getPage(1, 10));
        return "Index";
    }

//    @RequestMapping(method = RequestMethod.GET, value = "/{storyId}")
//    public String getDetail(@PathVariable("storyId") long storyId, Model model) {
//        model.addAttribute("detailStory", storyService.getDetail(storyId));
//        model.addAttribute("chapters", chapterService.getAllChapterByStory(storyId));
//        model.addAttribute("chapterService", chapterService.getDetail(storyId));
//        return "story/detail";
//
//    }

    @RequestMapping(method = RequestMethod.POST, value = "/create")
    public String store(@Valid Story story, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "story/form";
        }
        storyService.create(story);
        return "story/success";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/create")
    public String create(Model model) {
        model.addAttribute("storyCreate", new Story());
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categories", categories);
        return "story/form";
    }


    // list new by created



    // list category by storyid
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/list_category")
    public String showStoryByCategory(@PathVariable("id") long story_id, Model model) {
        Story story = storyService.getDetail(story_id);
        Set<Category> categorySet = story.getCategories();
        model.addAttribute("story", story);
        model.addAttribute("categorySet", categorySet);
        return "story/test";
    }

    // list chapter by storyId
    @RequestMapping(method = RequestMethod.GET, value = "/{storyId}")
    public String getChaptersByStory(@PathVariable("storyId") long storyId, Model model) {
        Optional<Story> storys = storyRepository.findById(storyId);
        model.addAttribute("story", storys.get());
        model.addAttribute("chapters", chapterService.getAllChapterByStory(storyId));
        return "story/detail";
    }


    // create chapterby storyId
    // create chapter
    @RequestMapping(value = "/{storyId}/list_chapter/add")
    public String createChapterByStoryId(Model model, @PathVariable("storyId") long storyId) {
        model.addAttribute("storyIdss", storyId);
        model.addAttribute("chapter", new Chapter());
        return "chapter/form";
    }

    @RequestMapping(value = "/{storyId}/list_chapter/add", method = RequestMethod.POST)
    public String createChapterByStoryId(Chapter chapter, @PathVariable("storyId") long storyId) {
        chapterService.createChapterByStoryId(storyId, chapter);
//        long id = categoryService.create(category);
        return "redirect:/story/{storyId}/list_chapter";
    }

    //get chapter
    @RequestMapping(value = "/{storyId}/chapter/{chapterId}")
    public String detailChapterByStoryId(Model model, @PathVariable("storyId") long storyId, @PathVariable("chapterId") long chapterId) {
        Optional<Story> story = storyRepository.findById(storyId);
        model.addAttribute("story", story.get());
        model.addAttribute("chapters", chapterService.findByIdAndStoryId(chapterId, storyId));
        return "story/chapter/detail";
    }

    @RequestMapping(method = RequestMethod.GET, value = "story_new")
    public String getListNew(Model model,
                             @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                             @RequestParam(name = ",limit", required = false, defaultValue = "10") int limit
    ) {
        Page<Story> storyPage = storyService.getListCreatedAt(page, limit);
        model.addAttribute("listStoryNew", storyPage.getContent());
        model.addAttribute("totalPage", storyPage.getTotalPages());
        model.addAttribute("totalElement", storyPage.getTotalElements());
        model.addAttribute("page", page);
        model.addAttribute("limit", limit);
        Category category1 = categoryService.getDetail(1);
        model.addAttribute("category1", category1);
        Category category11 = categoryService.getDetail(12);
        model.addAttribute("category11", category11);
        Category category12 = categoryService.getDetail(13);
        model.addAttribute("category12", category12);
        Category category13 = categoryService.getDetail(13);
        model.addAttribute("category13", category13);
        Category category15 = categoryService.getDetail(15);
        model.addAttribute("category15", category15);
        return "story/list_new";
    }


    //rate
    @RequestMapping(method = RequestMethod.GET, value = "story_hot")
    public String getListRate(Model model,
                              @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                              @RequestParam(name = ",limit", required = false, defaultValue = "12") int limit
    ) {
        Page<Story> storyPage = storyService.getListRate(page, limit);
        model.addAttribute("listStoryHot", storyPage.getContent());
        model.addAttribute("totalPage", storyPage.getTotalPages());
        model.addAttribute("totalElement", storyPage.getTotalElements());
        model.addAttribute("page", page);
        model.addAttribute("limit", limit);
        return "story/list_hot";
    }

    //hot (view)
    @RequestMapping(method = RequestMethod.GET, value = "story_good")
    public String getListView(Model model,
                              @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                              @RequestParam(name = ",limit", required = false, defaultValue = "12") int limit
    ) {
        Page<Story> storyPage = storyService.getListRate(page, limit);
        model.addAttribute("listStoryGood", storyPage.getContent());
        model.addAttribute("totalPage", storyPage.getTotalPages());
        model.addAttribute("totalElement", storyPage.getTotalElements());
        model.addAttribute("page", page);
        model.addAttribute("limit", limit);
        return "story/list_good";
    }

    //list chapter
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/list_chapter")
    public String getChaptersByStory(@PathVariable("id") Long storyId, Model model) {
        Optional<Story> storys = storyRepository.findById(storyId);
        model.addAttribute("story", storys.get());
        model.addAttribute("chapters", chapterService.getAllChapterByStory(storyId));
        return "story/list_chapter";
    }
}

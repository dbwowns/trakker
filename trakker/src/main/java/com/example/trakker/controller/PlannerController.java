package com.example.trakker.controller;

import com.example.trakker.item.HeartDTO;
import com.example.trakker.model.planner.dto.PlannerDTO;
import com.example.trakker.model.planner.dto.ScheduleDTO;
import com.example.trakker.service.heart.HeartService;
import com.example.trakker.service.planner.PlannerService;

import com.example.trakker.utils.PagingInfoVO;
import com.example.trakker.utils.ResponseResultList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("planner")
public class PlannerController {
	private static final Logger logger = LoggerFactory.getLogger(PlannerController.class);

	@Autowired
	private PlannerService plannerService;
	@Autowired
	private HeartService heartService;


	@PostMapping("/new")
	public String write(Model model,
						@RequestParam Map<String, Object> map) {
		String kName = plannerService.selectLocal(Integer.parseInt((String)map.get("planner-local")));

		model.addAttribute("days", map.get("planner-days"));
		model.addAttribute("lNum", map.get("planner-local"));
		model.addAttribute("kName", kName);
		model.addAttribute("title", map.get("planner-title"));
		model.addAttribute("memo", map.get("planner-memo"));

		return "planner/insert";
	}
	@PostMapping("/insert")
	public void insert(PlannerDTO planner,
					   @RequestParam(value = "sday[]") List<String> sday,
					   @RequestParam(value = "snum[]") List<String> snum,
					   @RequestParam(value = "spoint[]") List<String> spoint,
					   @RequestParam(value = "smemo[]") List<String> smemo,
					   @RequestParam(value = "y[]") List<String> y,
					   @RequestParam(value = "x[]") List<String> x) {
		plannerService.insert(planner, sday, snum, spoint, smemo, y, x);
	}


	@GetMapping
	public String list(Model model,
					   HttpSession session,
					   HttpServletRequest request,
					   @RequestParam(value = "page", required = false, defaultValue = "1") int page,
					   @RequestParam(value = "area",required = false, defaultValue = "0") Integer area,
					   @RequestParam(value = "sort",required = false, defaultValue = "add") String sort,
					   @RequestParam(value = "searchType",required = false, defaultValue = "") String searchType,
					   @RequestParam(value = "keyword",required = false, defaultValue = "") String keyword) {
		Long memNum = (Long) session.getAttribute("mem_num");
		if(memNum==null){
			memNum = 0L;
		}

		PagingInfoVO vo = new PagingInfoVO();
		vo.setPageNum(page);
		vo.setArea(area);
		vo.setSort(sort);
		vo.setStype(searchType);
		vo.setSdata(keyword);

		String urlCheck = request.getServletPath();
		ResponseResultList responseResultList = plannerService.list(vo, memNum, urlCheck);

		model.addAttribute("list", responseResultList.getBody());
		model.addAttribute("page", responseResultList.getMeta().get("pagingInfo"));
		model.addAttribute("select", page);
		model.addAttribute("local", plannerService.localList());
		model.addAttribute("area", area);
		model.addAttribute("sort", sort);
		model.addAttribute("type", searchType);
		model.addAttribute("keyword",keyword);

		return "planner/list";
	}


	@GetMapping("/{planNum}")
	public String detail(Model model,
						 HttpSession session,
						 HttpServletRequest request,
						 HttpServletResponse response,
						 @PathVariable("planNum") Long planNum) {
		Long memNum = (Long)session.getAttribute("mem_num");
		if(memNum==null){
			logger.info("로그인 값이 없습니다. 비회원 값을 부여합니다.");
			memNum = 0L;
		}
		plannerService.updateHit(planNum, request, response);
		ResponseResultList responseResultList = plannerService.detail(planNum, memNum);
		List<Integer> days = plannerService.getDayList((Integer)responseResultList.getMeta().get("days"));

		model.addAttribute("planner", responseResultList.getMeta());
		model.addAttribute("days", days);
		model.addAttribute("schedules", responseResultList.getBody());
		model.addAttribute("prev", responseResultList.getPrev());
		model.addAttribute("next", responseResultList.getNext());

		return "planner/detail";
	}

	@ResponseBody
	@PostMapping("/{planNum}")
	public void heart(HeartDTO heart) {
		heartService.heartCheck(heart);
	}


	@PostMapping("/edit/{planNum}")
	public String edit(Model model,
					   @PathVariable("planNum") Long planNum,
					   @RequestParam Map<String, Object> map) {
		List<ScheduleDTO> schedules = plannerService.selectEdit(planNum);

		model.addAttribute("listDays", map.get("listDays"));
		model.addAttribute("planNum", planNum);
		model.addAttribute("kname", map.get("planner-local"));
		model.addAttribute("days", map.get("planner-days"));
		model.addAttribute("title", map.get("planner-title"));
		model.addAttribute("memo", map.get("planner-memo"));
		model.addAttribute("schedules", schedules);

		return "planner/edit";
	}
	@ResponseBody
	@PostMapping("/rewrite")
	public void update(PlannerDTO planner,
					   @RequestParam(value = "sday[]") List<String> sday,
					   @RequestParam(value = "snum[]") List<String> snum,
					   @RequestParam(value = "spoint[]") List<String> spoint,
					   @RequestParam(value = "smemo[]") List<String> smemo,
					   @RequestParam(value = "y[]") List<String> y,
					   @RequestParam(value = "x[]") List<String> x) {
		plannerService.update(planner, sday, snum, spoint, smemo, y, x);
	}


	@ResponseBody
	@PostMapping("/delete/{planNum}")
	public void delete(@PathVariable Long planNum) {
		plannerService.delete(planNum);
	}
}

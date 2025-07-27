console.log("this is js");

const toggleSidebar = () =>{
	
	if($(".sidebar").is(":visible")){
		//true
		//band krna h 
		$(".sidebar").css("display", "none")
		$(".content").css("margin-left", "0%")
	}else{
		//false
		//dikhana h 
		$(".sidebar").css("display", "block")
		$(".content").css("margin-left", "20%")
	}
}
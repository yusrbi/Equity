<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>

<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="css/main.css">

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Equity - Disambiguation Page</title>

<script src="js/jquery-3.1.0.min.js"></script>
<script src="js/colResizable-1.6.min.js"></script>
<script src="js/helper.js"></script>

<script type="text/javascript">
	$(function(){	

		//callback function
		var onSlide = function(e){
			var columns = $(e.currentTarget).find("td");
			var ranges = [], total = 0, i, s ="Selected Hyper-params: ", w;
			for(i = 0; i<columns.length; i++){
				w = columns.eq(i).width()-10 - (i==0?1:0);
				ranges.push(w);
				total+=w;
			}		 
			for(i=0; i<columns.length; i++){			
				ranges[i] = 100*ranges[i]/total;
				carriage = ranges[i]-w
				s+=" "+ Math.round(ranges[i]) + "%,";			
			}		
			s=s.slice(0,-1);			
			$("#hp_text").html(s);
		}
		
		//colResize the table
		$("#hp_range").colResizable({
			liveDrag:true, 
			draggingClass:"rangeDrag", 
			gripInnerHtml:"<div class='rangeGrip'></div>", 
			onResize:onSlide,
			minWidth:10
			});
	
	});	
  </script>


<script>


		$(document).ready(function() {
		    $('div.go button').each(function (index) {
		        $(this).ajaxStart(function () {
		            $("#ajaxLoad").show();
		            $(this).prop('disabled', true);
		        });
		        $(this).ajaxStop(function () {
		            $("#ajaxLoad").hide();
		            $(this).prop('disabled', false);
		        });
		    });
			$('#table').val('Vehicle\tManufacturer\tClass\tGHG emissions\tTailpipe emissions (g\/mi of CO2)\tEPA Fuel Economy combined  (MPG)\tAnnual Fuel Cost\r\nToyota Prius PHEV\tToyota\tHybrid electric\t61 lb CO2\t133\t95 MPGe\t600\r\n \"Toyota Prius Eco- All years, gasoline fuel\"\tToyota\tHybrid electric\t51\t178\t50 mpg  (21.25 km\/l)\t600\r\n\"BMW i3 - All years, all fuels\"\tBMW\tElectric car\t54\t0\t124 MPGe (27 kWH\/100mi)\t<550$\r\nTesla Model S -2013 Award (60\/85kWh battery)\tTesla\tElectric car\t54\t0\t95 MPGe( 35 kWH\/100mi)\t700\r\nChevrolet Volt- 2011 Award\tGM\tPlug-in Hybrid\t61L/100km\t81\t98 MPGe (35kWh\/100mi)\t800\r\nBolloré Bluecar\tCecomp\tElectric car\t152 g\/km\t0\t \t900€');
			$('#context').val('The most efficient cars on the market are all electric cars. In fact, every electric car on the market is more efficient than even the most efficient conventional hybrid car (the Toyota Prius). Some of them are more than twice as efficient. As you scroll through the list below, note that the Prius has a MPG rating of 50 while Model S has a MPGe of 95. \nIf you are not familiar with MPGe, it is a rating created by the EPA to determine the relative efficiency of an electric car compared to a gasoline car. MPGe is generally good for comparing electric cars to conventional gasmobiles and hybrids');
		});
        $(document).delegate('#table', 'keydown', function(e) {
        	  var keyCode = e.keyCode || e.which;

        	  if (keyCode == 9) {
        	    e.preventDefault();
        	    var start = $(this).get(0).selectionStart;
        	    var end = $(this).get(0).selectionEnd;

        	    // set textarea value to: text before caret + tab + text after caret
        	    $(this).val($(this).val().substring(0, start)
        	                + "\t"
        	                + $(this).val().substring(end));

        	    // put caret at right position again
        	    $(this).get(0).selectionStart =
        	    $(this).get(0).selectionEnd = start + 1;
        	  }
        	});
            var ctxPath = "<%=request.getContextPath()%>";
	$(function() {
		$("#postDocument")
				.on(
						"click",
						function() {
							if ($('#table').val() == '') {
								alert('Please insert your table!');
								return

							}
							var form_data = new Object();
							var hyper_params = getHyperParams();

							form_data["context"] = $.trim($('#context').val());
							form_data["table_content"] = $.trim($('#table')
									.val());
							form_data["table_content"] = "\""
									+ form_data["table_content"].replace(/\n/g,
											"\"\n\"") + "\"";
							form_data["table_content"] = form_data["table_content"]
									.replace(/\t/g, "\"\t\"");
							form_data["table_content"] = form_data["table_content"]
									.replace(/\t/g, ",");
							form_data["max_itr"] = parseInt($('#max_iterations')
									.val());
							form_data["gamma"] = parseFloat($('#gamma_txt')
									.val());
							form_data["hp_same_string"] = parseFloat(hyper_params[0]
									.toFixed(6));
							form_data["hp_same_row"] = parseFloat(hyper_params[1]
									.toFixed(6));
							form_data["hp_same_column"] = parseFloat(hyper_params[2]
									.toFixed(6));
							form_data["hp_header_cell"] = parseFloat(hyper_params[3]
									.toFixed(6));
							form_data["hp_mention_candidate"] = parseFloat(hyper_params[4]
									.toFixed(6));
							form_data["hp_candidate_candidate"] = parseFloat(hyper_params[5]
									.toFixed(6));
							$(".loadicon").show();
					        $(".go").hide();
							$.ajax({
								url : ctxPath
										+ "/service/document/disambiguate",
								type : "POST",
								data : JSON.stringify(form_data),
								contentType : "application/json",
								cache : false,
								dataType : "json",
								success : function(response) {
									$(".loadicon").hide();
									$(".go").show();
							        					       
									$('#doc_results').html(
											response["document_html"])
									$('#table_results_head').html(
											response["table_header"])
									$('#table_results_body').html(
											response["table_body"])

								}
							});
						});
	});

	function updateTextInput(val, input_id) {
		document.getElementById(input_id).value = val;
	}
	function updateHyperParams(val) {
		total = 0;
		total += document.getElementById('').value;

	}

	function getHyperParams() {
		var columns = $('#hp_range').find("td");
		var ranges = [], total = 0, i;
		for (i = 0; i < columns.length; i++) {
			w = columns.eq(i).width() - 10 - (i == 0 ? 1 : 0);
			ranges.push(w);
			total += w;
		}
		for (i = 0; i < columns.length; i++) {
			ranges[i] = ranges[i] / total;
		}
		return ranges;
	}
</script>
</head>

<body>
	<div class="container">
		<form class="go-bottom">
			<h2>Input Document</h2>
			<div>
				<textarea id="table" rows="20" cols="40" required></textarea>
				<label for="table">Table</label>
			</div>
			<h3></h3>
			<div>
				<textarea id="context" rows="10" cols="40" required></textarea>

				<label for="context">Table Context</label>
			</div>


		</form>

		<form class="go-bottom">
			<h2>Settings</h2>
			<div>
				<input id="gamma" type=range step=0.01 min=0.0 max=1.0 value="0.9"
					onchange="updateTextInput(this.value, 'gamma_txt');" /> <input
					type="number" id="gamma_txt" value="0.9" min=0.0 max=1.0 step=0.01
					required> <label for="gamma_txt">Damping Factor</label>
			</div>
			<h3></h3>
			<div>
				<input type="number" id="max_iterations" value=50 step=5 max=200
					required> <label for="max_iterations">Maximum
					Iterations</label>
			</div>
			<h3>Hyper-parameters</h3>


			<div class="center">
				<br /> <br />

				<div id="slider">
					<table id="hp_range">
						<tr height="60px">
							<th>|same-string</th>
							<th>|same-row</th>
							<th>|same-column</th>
							<th>|header-cell</th>
							<th>|mention-cand</th>
							<th>|cand-cand</th>
						</tr>
						<tr>
							<td width="16.6%"></td>
							<td width="16.6%"></td>
							<td width="16.6%"></td>
							<td width="16.6%"></td>
							<td width="16.6%"></td>
							<td width="16.6%"></td>
						</tr>
					</table>
				</div>

				<p id="hp_text">Selected Hyper-params: 16.6%, 16.6%, 16.6%,
					16.6%, 16.6%, 16.6%</p>

				<br /> <br />

			</div>
			<h3></h3>

		</form>
		<div class="loadicon" hidden="true" style=" position: relative;float: left;top:500px;" >
			<img id="ajaxLoad" alt="Loading ...." src="img/load.gif" />		
		</div>
		<div class="go">			
			<button id="postDocument" class="btn">Post Document</button>
		</div>
		<div class="results">


			<div id="doc_results"></div>
			<table id="table_results">
				<thead id="table_results_head">

				</thead>
				<tbody id="table_results_body">

				</tbody>
			</table>

		</div>

	</div>
</body>

</html>

<!-- 
		<div>
			<input id="hp_same_string" type=range step=0.01 min=0.0 max=1.0 value="0.2"
				onchange="updateTextInput(this.value, 'hp_same_string_txt');" />
			 <input type="number" onchange="updateHyperParams(this.value);"
				id="hp_same_string_txt" value="0.2" min=0.0 max=1.0 step=0.01 required>
			<label for="hp_same_string_txt">Same String</label>
		</div>
		<div>
			<input id="hp_same_row" type=range step=0.01 min=0.0 max=1.0 value="0.2"
				onchange="updateTextInput(this.value, 'hp_same_row_txt');" />
			 <input type="number" onchange="updateHyperParams(this.value);"
				id="hp_same_row_txt" value="0.2" min=0.0 max=1.0 step=0.01 required>
			<label for="hp_same_row_txt">Same Row</label>
		</div>
		<div>
			<input id="hp_same_column" type=range step=0.01 min=0.0 max=1.0 value="0.2"
				onchange="updateTextInput(this.value, 'hp_same_column_txt');" />
			 <input type="number" onchange="updateHyperParams(this.value);"
				id="hp_same_column_txt" value="0.2" min=0.0 max=1.0 step=0.01 required>
			<label for="hp_same_column_txt">Same Column</label>
		</div>
		<div>
			<input id="hp_header_cell" type=range step=0.01 min=0.0 max=1.0 value="0.1"
				onchange="updateTextInput(this.value, 'hp_header_cell_txt');" />
			 <input type="number" onchange="updateHyperParams(this.value);"
				id="hp_header_cell_txt" value="0.2" min=0.0 max=1.0 step=0.01 required>
			<label for="hp_header_cell_txt">Header-Cell</label>
		</div>
		<div>
			<input id="hp_mention_candidate" type=range step=0.01 min=0.0 max=1.0 value="0.2"
				onchange="updateTextInput(this.value, 'hp_mention_candidate_txt');" />
			 <input type="number" onchange="updateHyperParams(this.value);"
				id="hp_mention_candidate_txt" value="0.2" min=0.0 max=1.0 step=0.01 required>
			<label for="hp_mention_candidate_txt">Mention-Candidate</label>
		</div>
		<div>
			<input id="hp_candidate_candidate" type=range step=0.01 min=0.0 max=1.0 value="0.1"
				onchange="updateTextInput(this.value, 'hp_candidate_candidate_txt');" />
			 <input type="number" onchange="updateHyperParams(this.value);"
				id="hp_candidate_candidate_txt" value="0.2" min=0.0 max=1.0 step=0.01 required>
			<label for="hp_candidate_candidate_txt">Candidate-Candidate</label>
		</div>
		 -->
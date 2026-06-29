document.addEventListener("DOMContentLoaded", function () {
	var form = document.querySelector(".review_form");
	if (!form) {
		return;
	}

	var ratingInputs = Array.prototype.slice.call(form.querySelectorAll('input[name="rating"]'));
	var labels = ratingInputs.map(function (input) {
		return form.querySelector('label[for="' + input.id + '"]');
	});
	var table = form.querySelector(".review_input_table");
	var status = document.createElement("p");
	status.className = "review_rating_status";
	status.setAttribute("aria-live", "polite");

	if (table) {
		table.parentNode.insertBefore(status, table.nextSibling);
	}

	function updateSelectedRating() {
		var selected = ratingInputs.find(function (input) {
			return input.checked;
		});
		var selectedValue = selected ? Number(selected.value) : 0;

		ratingInputs.forEach(function (input, index) {
			var label = labels[index];
			if (!label) {
				return;
			}
			var value = Number(input.value);
			label.classList.toggle("is-selected", selectedValue > 0 && value <= selectedValue);
		});

		status.textContent = selectedValue > 0 ? selectedValue + " / 5" : "";
	}

	ratingInputs.forEach(function (input) {
		input.addEventListener("change", updateSelectedRating);
	});
	updateSelectedRating();
});

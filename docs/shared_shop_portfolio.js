(function () {
	"use strict";

	var nav = document.getElementById("siteNav");
	var navToggle = document.querySelector(".nav-toggle");
	var navLinks = Array.prototype.slice.call(document.querySelectorAll(".site-nav a"));
	var sections = navLinks
		.map(function (link) { return document.querySelector(link.getAttribute("href")); })
		.filter(Boolean);

	if (navToggle && nav) {
		navToggle.addEventListener("click", function () {
			var isOpen = nav.classList.toggle("is-open");
			navToggle.setAttribute("aria-expanded", String(isOpen));
		});
		navLinks.forEach(function (link) {
			link.addEventListener("click", function () {
				nav.classList.remove("is-open");
				navToggle.setAttribute("aria-expanded", "false");
			});
		});
	}

	if ("IntersectionObserver" in window) {
		var revealObserver = new IntersectionObserver(function (entries) {
			entries.forEach(function (entry) {
				if (entry.isIntersecting) {
					entry.target.classList.add("is-visible");
					revealObserver.unobserve(entry.target);
				}
			});
		}, { threshold: 0.14 });

		document.querySelectorAll(".reveal").forEach(function (node) {
			revealObserver.observe(node);
		});

		var sectionObserver = new IntersectionObserver(function (entries) {
			entries.forEach(function (entry) {
				if (!entry.isIntersecting) {
					return;
				}
				var id = "#" + entry.target.id;
				navLinks.forEach(function (link) {
					link.classList.toggle("is-active", link.getAttribute("href") === id);
				});
			});
		}, { rootMargin: "-42% 0px -48% 0px" });

		sections.forEach(function (section) {
			sectionObserver.observe(section);
		});
	} else {
		document.querySelectorAll(".reveal").forEach(function (node) {
			node.classList.add("is-visible");
		});
	}

	var filterButtons = Array.prototype.slice.call(document.querySelectorAll("[data-filter]"));
	var galleryItems = Array.prototype.slice.call(document.querySelectorAll(".gallery figure"));
	filterButtons.forEach(function (button) {
		button.addEventListener("click", function () {
			var filter = button.getAttribute("data-filter");
			filterButtons.forEach(function (item) {
				item.classList.toggle("is-active", item === button);
			});
			galleryItems.forEach(function (item) {
				var show = filter === "all" || item.getAttribute("data-group") === filter;
				item.classList.toggle("is-hidden", !show);
			});
		});
	});

	var lightbox = document.getElementById("lightbox");
	var lightboxImage = lightbox ? lightbox.querySelector("img") : null;
	var lightboxClose = lightbox ? lightbox.querySelector("button") : null;

	function closeLightbox() {
		if (!lightbox || !lightboxImage) {
			return;
		}
		lightbox.classList.remove("is-open");
		lightbox.setAttribute("aria-hidden", "true");
		lightboxImage.src = "";
		lightboxImage.alt = "";
	}

	document.querySelectorAll("[data-lightbox]").forEach(function (image) {
		image.addEventListener("click", function () {
			if (!lightbox || !lightboxImage) {
				return;
			}
			lightboxImage.src = image.src;
			lightboxImage.alt = image.alt || "";
			lightbox.classList.add("is-open");
			lightbox.setAttribute("aria-hidden", "false");
			if (lightboxClose) {
				lightboxClose.focus();
			}
		});
	});

	if (lightbox) {
		lightbox.addEventListener("click", function (event) {
			if (event.target === lightbox) {
				closeLightbox();
			}
		});
	}

	if (lightboxClose) {
		lightboxClose.addEventListener("click", closeLightbox);
	}

	document.addEventListener("keydown", function (event) {
		if (event.key === "Escape") {
			closeLightbox();
		}
	});
}());

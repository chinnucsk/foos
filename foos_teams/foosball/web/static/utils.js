$.fn.myanimate = function(finalvalue) {
  this.each(function() {
    var $this = $(this);
	jQuery({someValue: 0}).animate({someValue: finalvalue}, {
		duration: 500,
		easing:'swing', // can be anything
		step: function() { // called on every step
			// Update the element's text with rounded-up value:
			//$('#el').text(Math.ceil(this.someValue) + "%");
			$this.text(Math.ceil(this.someValue) + "");
			}
		});
	});
	  return this;
};
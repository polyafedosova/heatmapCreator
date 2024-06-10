(function($) {
  $(document).ready(function() {
    // Enable Popovers and Tooltips
    $('[data-toggle="popover"]').popover();
    $('[data-toggle="tooltip"]').tooltip();
    // Datepicker Example 1
    $('#datepicker-example-1').datepicker({});
    // Datepicker Example 2
    $('#datepicker-example-2').datepicker({
      calendarWeeks: true,
      autoclose: true,
      todayHighlight: true
    });
    // Datepicker Example 3
    $('#datepicker-example-3').datepicker();
    // Slider Example 1 - Default Slider
    $('#shards-slider-example-1').customSlider({
      start: [25],
      connect: true,
      range: {
        'min': 0,
        'max': 100
      }
    });
    // Slider Example 2 - Slider with multiple handles
    jQuery('#shards-slider-example-2').customSlider({
      start: [25, 70],
      connect: true,
      range: {
        'min': 0,
        'max': 100
      }
    });
    // Slider Example 3 - Slider with tooltips
    jQuery('#shards-slider-example-3').customSlider({
      start: [25, 50],
      tooltips: [true, true],
      connect: true,
      range: {
        'min': 0,
        'max': 100
      }
    });
    // Slider Example 4 - Slider with both tooltips and pips.
    jQuery('#shards-slider-example-4').customSlider({
      start: [25, 50],
      tooltips: [true, true],
      connect: true,
      range: {
        'min': 0,
        'max': 100
      },
      pips: {
        mode: 'positions',
        values: [0, 25, 50, 75, 100],
        density: 5
      }
    });
  });
})(jQuery);

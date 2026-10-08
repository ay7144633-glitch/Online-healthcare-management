document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('[data-availability-form]');
    if (!form) {
        return;
    }

    const startInput = form.querySelector('[name="availableStart"]');
    const endInput = form.querySelector('[name="availableEnd"]');
    const toMinutes = (value) => {
        const [hours, minutes] = value.split(':').map(Number);
        return hours * 60 + minutes;
    };
    const formatTime = (minutes) => {
        const hours = Math.floor(minutes / 60).toString().padStart(2, '0');
        const remainder = (minutes % 60).toString().padStart(2, '0');
        return `${hours}:${remainder}`;
    };
    const updateEndMinimum = () => {
        if (!startInput.value) {
            endInput.removeAttribute('min');
            return;
        }
        const minimumMinutes = toMinutes(startInput.value) + 30;
        if (minimumMinutes >= 24 * 60) {
            endInput.value = '';
            endInput.setCustomValidity('Choose a start time before 11:30 PM.');
            return;
        }
        endInput.setCustomValidity('');
        endInput.min = formatTime(minimumMinutes);
        if (endInput.value && toMinutes(endInput.value) < minimumMinutes) {
            endInput.value = '';
        }
    };

    startInput.addEventListener('input', updateEndMinimum);
    startInput.addEventListener('change', updateEndMinimum);
    endInput.addEventListener('input', () => {
        if (startInput.value && endInput.value
                && toMinutes(endInput.value) - toMinutes(startInput.value) < 30) {
            endInput.setCustomValidity('Choose an end time at least 30 minutes after the start.');
        } else {
            endInput.setCustomValidity('');
        }
    });

    form.querySelectorAll('[data-start][data-end]').forEach((preset) => {
        preset.addEventListener('click', () => {
            startInput.value = preset.dataset.start;
            endInput.value = preset.dataset.end;
            updateEndMinimum();
            endInput.dispatchEvent(new Event('input', { bubbles: true }));
        });
    });

    form.addEventListener('submit', (event) => {
        if (!startInput.value || !endInput.value
                || toMinutes(endInput.value) - toMinutes(startInput.value) < 30) {
            event.preventDefault();
            endInput.setCustomValidity('Choose an end time at least 30 minutes after the start.');
            endInput.reportValidity();
        }
    });
});

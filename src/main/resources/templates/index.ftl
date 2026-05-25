<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Keeki Booth</title>
    <link rel="icon" type="image/jpeg" href="/favicon.jpg">
    <link rel="stylesheet" href="/style.css">
</head>
<body>
<header>
    <img src="/keeki-banner.png" alt="Keeki Booth" class="banner">
    <h2>${date}</h2>
    <nav class="menu">
        <button class="burger"
                onmousedown="startBurgerPress(this)" onmouseup="cancelBurgerPress(this)" onmouseleave="cancelBurgerPress(this)"
                ontouchstart="startBurgerPress(this)" ontouchend="cancelBurgerPress(this)">Menu</button>
        <div class="menu-dropdown">
            <a href="#" onclick="openDatePicker(); return false;">Change date</a>
            <a href="/admin">Admin</a>
        </div>
    </nav>
</header>

<main class="times">
    <#list blocks as block>

        <div class="time-block <#if block.taken == 0>free<#elseif block.taken == block.total>full<#else>partial</#if>"
             <#if block.taken != block.total>onclick="openBooking('${block.time}')"</#if>>

            <span class="block-time">${block.time}</span>

            <div>
                <#if block.taken == block.total>
                    Booked out
                <#else>
                    ${block.total - block.taken} slots free
                </#if>
            </div>
            <div class="progress-bar">
                <div class="progress-fill" style="width: ${block.pct}%"></div>
            </div>



        </div>
    </#list>
</main>

<dialog id="date-dialog">
    <h3>Change date</h3>
    <input id="date-input" type="date" value="${isoDate}">
    <div class="dialog-actions">
        <button class="cancel" onclick="document.getElementById('date-dialog').close()">Cancel</button>
        <button class="cancel" onclick="window.location.href='/'">Today</button>
        <button class="book" onclick="goToDate()">Go</button>
    </div>
</dialog>

<dialog id="booking-dialog">
    <div id="booking-form">
        <h3 id="dialog-time"></h3>
        <label for="booking-name">Name</label>
        <input id="booking-name" type="text" placeholder="Your name" required autofocus>
        <div class="dialog-actions">
            <button class="cancel" onclick="document.getElementById('booking-dialog').close()">Cancel</button>
            <button class="book" onclick="confirmBooking()">Book</button>
        </div>
    </div>
    <div id="booking-confirmation" hidden>
        <p class="confirmation-message">Return to attendant</p>
        <p id="date-warning" class="date-warning" hidden>Warning: booking for ${date}, not today.</p>
        <div class="dialog-actions">
            <button class="confirm"
                    onmousedown="startPress(this)" onmouseup="cancelPress(this)" onmouseleave="cancelPress(this)"
                    ontouchstart="startPress(this)" ontouchend="cancelPress(this)">
                Confirm booking
            </button>
        </div>
    </div>
</dialog>

<script>
    const pageDate = '${isoDate}';
    const today = new Date().toISOString().slice(0, 10);

    let pressTimer;
    let currentTime = '';
    let currentName = '';

    function openBooking(time) {
        currentTime = time;
        document.getElementById('dialog-time').textContent = time;
        document.getElementById('booking-name').value = '';
        document.getElementById('booking-form').hidden = false;
        document.getElementById('booking-confirmation').hidden = true;
        document.getElementById('booking-dialog').showModal();
    }

    function confirmBooking() {
        const nameInput = document.getElementById('booking-name');
        if (!nameInput.value.trim()) {
            nameInput.reportValidity();
            return;
        }
        currentName = nameInput.value.trim();
        document.getElementById('date-warning').hidden = pageDate === today;
        document.getElementById('booking-form').hidden = true;
        document.getElementById('booking-confirmation').hidden = false;
    }

    function startPress(btn) {
        btn.classList.add('pressing');
        pressTimer = setTimeout(() => {
            fetch('/book', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ name: currentName, time: currentTime, date: pageDate })
            })
            .then(r => {
                if (!r.ok) throw new Error('Booking failed');
                return r.json();
            })
            .then(() => {
                document.getElementById('booking-dialog').close();
                location.reload();
            })
            .catch(() => {
                alert('Booking failed. The slot may be full.');
                btn.classList.remove('pressing');
            });
        }, 800);
    }

    function cancelPress(btn) {
        clearTimeout(pressTimer);
        btn.classList.remove('pressing');
    }

    let burgerTimer;

    function startBurgerPress(btn) {
        btn.classList.add('pressing');
        burgerTimer = setTimeout(() => {
            btn.classList.remove('pressing');
            btn.closest('.menu').classList.toggle('open');
        }, 1600);
    }

    function cancelBurgerPress(btn) {
        clearTimeout(burgerTimer);
        btn.classList.remove('pressing');
    }

    function openDatePicker() {
        document.querySelector('.menu').classList.remove('open');
        document.getElementById('date-dialog').showModal();
    }

    function goToDate() {
        const date = document.getElementById('date-input').value;
        if (date) window.location.href = '/?date=' + date;
    }

    document.addEventListener('click', e => {
        if (!e.target.closest('.menu')) {
            document.querySelector('.menu')?.classList.remove('open');
        }
    });
</script>

</body>
</html>

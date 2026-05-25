<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Keeki Booth – Admin</title>
    <link rel="icon" type="image/jpeg" href="/favicon.jpg">
    <link rel="stylesheet" href="/style.css">
</head>
<body>
<header>
    <img src="/keeki-banner.png" alt="Keeki Booth" class="banner">
    <h2>${dateFormatted}</h2>
    <nav class="menu">
        <button class="burger" onclick="this.closest('.menu').classList.toggle('open')">Menu</button>
        <div class="menu-dropdown">
            <a href="/">Back to queue</a>
        </div>
    </nav>
</header>

<main>
    <form class="date-picker" method="get" action="/admin">
        <label for="date">Date</label>
        <input id="date" type="date" name="date" value="${date}" onchange="this.form.submit()">
    </form>

    <#assign totalTaken = 0>
    <#assign totalSlots = 0>
    <#assign freeCount = 0>
    <#assign partialCount = 0>
    <#assign fullCount = 0>
    <#list blocks as block>
        <#assign totalTaken = totalTaken + block.taken>
        <#assign totalSlots = totalSlots + block.total>
        <#if block.taken == 0>
            <#assign freeCount = freeCount + 1>
        <#elseif block.taken == block.total>
            <#assign fullCount = fullCount + 1>
        <#else>
            <#assign partialCount = partialCount + 1>
        </#if>
    </#list>
    <#assign dayPct = (totalSlots > 0)?then(totalTaken * 100 / totalSlots, 0)>

    <section class="overview">
        <div class="overview-nums">
            <span><strong>${totalTaken}</strong>booked</span>
            <span><strong>${totalSlots - totalTaken}</strong>free</span>
            <span><strong>${totalSlots}</strong>capacity</span>
        </div>
        <div class="progress-bar large">
            <div class="progress-fill" style="width: ${dayPct}%"></div>
        </div>
        <div class="overview-tags">
            <span class="tag free">${freeCount} free</span>
            <span class="tag partial">${partialCount} partial</span>
            <span class="tag full">${fullCount} full</span>
        </div>
    </section>

    <div class="admin-list">
        <#list blocks as block>
            <div class="time-block admin-block <#if block.taken == 0>free<#elseif block.taken == block.total>full<#else>partial</#if>">
                <div class="admin-block-header">
                    <span class="block-time">${block.time}</span>
                    <span class="slot-count">${block.taken} / ${block.total}</span>
                </div>

                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${block.pct}%"></div>
                </div>

                <#if block.slots?has_content>
                    <ul class="booking-list">
                        <#list block.slots as slot>
                            <li class="booking-row">
                                <span class="booking-name">${slot.name}</span>
                                <span class="booking-code">${slot.code}</span>
                                <form method="post" action="/booking/delete">
                                    <input type="hidden" name="date" value="${date}">
                                    <input type="hidden" name="time" value="${block.time}">
                                    <input type="hidden" name="code" value="${slot.code}">
                                    <button type="submit" class="delete"
                                            onclick="return confirm('Delete booking for ${slot.name}?')">&#x2715;</button>
                                </form>
                            </li>
                        </#list>
                    </ul>
                <#else>
                    <p class="empty">No bookings yet</p>
                </#if>
            </div>
        </#list>
    </div>
</main>

<script>
    document.addEventListener('click', e => {
        if (!e.target.closest('.menu')) {
            document.querySelector('.menu')?.classList.remove('open');
        }
    });
</script>

</body>
</html>

const SEQUENCEFLOW_RADIUS = 30;
const SEQUENCEFLOW_STEPWIDTH = 10;
const SEQUENCEFLOW_VALUE_MODIFIER = 0.2;
const ACTIVITY_DENSITY = 20;
const ACTIVITY_RADIUS = 50;
const ACTIVITY_VALUE_MODIFIER = 0.125;
const VALUE_SHIFT = 0.17;
const COOLNESS = 2.5;
const EDGE_BUFFER = 75;
const RESOLUTION = 4;

export function getHeatmap(viewer, data, noSequenceHighlight) {
    const heat = generateHeatmap(viewer, data, noSequenceHighlight);
    const node = document.createElementNS('http://www.w3.org/2000/svg', 'image');
    Object.keys(heat.dimensions).forEach((prop) => {
        node.setAttributeNS(null, prop, heat.dimensions[prop]);
    });
    node.setAttributeNS('http://www.w3.org/1999/xlink', 'xlink:href', heat.img);
    node.setAttributeNS(null, 'style', 'opacity: 0.8; pointer-events: none;');
    return node;
}
function generateHeatmap(viewer, data, noSequenceHighlight) {
    const dimensions = getDimensions(viewer);
    // Проверка, что data является объектом
    if (typeof data !== 'object' || data === null || Array.isArray(data)) {
        console.error('Ожидались данные в виде объекта, получен:', data);
        return; // Прекращение выполнения функции, если данные неверны
    }
    let heatmapData = generateData(data, viewer, dimensions, noSequenceHighlight);
    // Проверяем, есть ли ключ 'Threshold' в данных
    let heatmapDataValueMax;
    if ('Threshold' in data) {
        heatmapDataValueMax = data.Threshold;
        console.log('МАКСИМУМ из Threshold:', heatmapDataValueMax);
    } else {
        heatmapDataValueMax = Math.max(...heatmapData.map(el => el.value));
        console.log('МАКСИМУМ из heatmapData:', heatmapDataValueMax);
    }
    heatmapData = heatmapData.map(({x, y, value, radius}) => {
        const shiftValue = noSequenceHighlight ? 0 : VALUE_SHIFT;
        return {
            x: Math.round(x),
            y: Math.round(y),
            radius,
            value: (shiftValue + (value / heatmapDataValueMax) * (1 - shiftValue)) / COOLNESS,
        };
    });
    const map = createMap(dimensions);
    map.setData({
        min: 0,
        max: 1,
        data: heatmapData,
    });
    return {
        img: map.getDataURL(),
        dimensions,
    };
}
function getDimensions(viewer) {
    const dimensions = viewer.get('canvas').getActiveLayer().getBBox();
    return {
        width: dimensions.width + 2 * EDGE_BUFFER,
        height: dimensions.height + 2 * EDGE_BUFFER,
        x: dimensions.x - EDGE_BUFFER,
        y: dimensions.y - EDGE_BUFFER,
    };
}
function createMap(dimensions) {
    const container = document.createElement('div');
    container.style.width = `${dimensions.width / RESOLUTION}px`;
    container.style.height = `${dimensions.height / RESOLUTION}px`;
    container.style.position = 'absolute';
    container.style.top = '0';
    container.style.left = '0';
    container.style.visibility = 'hidden';
    document.body.appendChild(container);
    const map = h337.create({
        container: container,
        radius: ACTIVITY_RADIUS / RESOLUTION,
        maxOpacity: .6,
        minOpacity: 0,
        blur: .75
    });
    document.body.removeChild(container);
    console.log('Контейнер для карты создан, размеры:', dimensions);
    return map;
}
function isBpmnType(element, types) {
    if (typeof types === 'string') {
        types = [types];
    }
    return (
        element.type !== 'label' &&
        types.some(type => element.businessObject.$instanceOf('bpmn:' + type))
    );
}
function isExcluded(element) {
    return isBpmnType(element, 'SubProcess') && !element.collapsed;
}
function generateData(values, viewer, {x: xOffset, y: yOffset}, noSequenceHighlight) {
    console.log('Полученные данные:', values); // Добавьте эту строку
    const data = [];
    const elementRegistry = viewer.get('elementRegistry');
    for (const key in values) {
        const element = elementRegistry.get(key);
        if (!element || typeof values[key] !== 'number') {
            continue;
        }
        if (!isExcluded(element)) {
            for (let i = 0; i < element.width + ACTIVITY_DENSITY / 2; i += ACTIVITY_DENSITY) {
                for (let j = 0; j < element.height + ACTIVITY_DENSITY / 2; j += ACTIVITY_DENSITY) {
                    const value = values[key] === 0 ? Number.EPSILON : values[key];
                    data.push({
                        x: (element.x + i - xOffset) / RESOLUTION,
                        y: (element.y + j - yOffset) / RESOLUTION,
                        value: value * ACTIVITY_VALUE_MODIFIER,
                        radius: ACTIVITY_RADIUS / RESOLUTION,
                    });
                }
            }
        }
        if (!noSequenceHighlight) {
            element.incoming.forEach(flow => {
                drawSequenceFlow(data, flow.waypoints, Math.min(values[key], values[flow.source.id]), {xOffset, yOffset});
            });
        }
    }
    console.log('Сгенерированные данные для тепловой карты:', data);
    return data;
}
function drawSequenceFlow(data, waypoints, value, {xOffset, yOffset}) {
    if (!value) return;
    for (let i = 1; i < waypoints.length; i++) {
        const start = waypoints[i - 1];
        const end = waypoints[i];
        const movementVector = {
            x: end.x - start.x,
            y: end.y - start.y,
        };
        const normalizedMovementVector = {
            x: (movementVector.x / (Math.abs(movementVector.x) + Math.abs(movementVector.y))) * SEQUENCEFLOW_STEPWIDTH,
            y: (movementVector.y / (Math.abs(movementVector.x) + Math.abs(movementVector.y))) * SEQUENCEFLOW_STEPWIDTH,
        };
        const numberSteps = Math.sqrt(movementVector.x ** 2 + movementVector.y ** 2) / SEQUENCEFLOW_STEPWIDTH;
        for (let j = 0; j < numberSteps; j++) {
            data.push({
                x: (start.x + normalizedMovementVector.x * j - xOffset) / RESOLUTION,
                y: (start.y + normalizedMovementVector.y * j - yOffset) / RESOLUTION,
                value: value * SEQUENCEFLOW_VALUE_MODIFIER,
                radius: SEQUENCEFLOW_RADIUS / RESOLUTION,
            });
        }
    }
}

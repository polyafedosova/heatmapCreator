import { getHeatmap } from './map.js';

let viewer;
let currentHeatmapOverlay;

class HeatmapOverlay {
    constructor(viewer, data, options = {}) {
        this.viewer = viewer;
        this.data = data;
        this.options = options;
        this.heatmap = undefined;
        this.init();
    }
    init() {
        this.renderHeatmap();
        this.setupEventListeners();
    }
    setupEventListeners() {
        const { onNodeClick } = this.options;
        if (onNodeClick) {
            this.viewer.get('eventBus').on('element.click', event => {
                if (onNodeClick) onNodeClick(event);
            });
            this.indicateClickableNodes();
        }
    }
    indicateClickableNodes() {
        if (this.data) {
            Object.keys(this.data).forEach(id => {
                const node = document.body.querySelector(`[data-element-id="${id}"]`);
                if (node) {
                    node.classList.add('clickable');
                }
            });
        }
    }
    renderHeatmap() {
        const heatmap = getHeatmap(this.viewer, this.data, this.options.noSequenceHighlight);
        if (this.heatmap) {
            this.viewer.get('canvas')._viewport.removeChild(this.heatmap);
        }
        this.viewer.get('canvas')._viewport.appendChild(heatmap);
        this.heatmap = heatmap;
    }
    destroy() {
        if (this.viewer && this.viewer.get('canvas') && this.viewer.get('canvas')._viewport && this.heatmap && this.viewer.get('canvas')._viewport.contains(this.heatmap)) {
            this.viewer.get('canvas')._viewport.removeChild(this.heatmap);
        }
        this.heatmap = undefined;
    }
}

async function fetchHeatmapData(processDefinitionId, type, filters) {
    let queryParameters = new URLSearchParams();
    if (filters.activityTypes) {
        filters.activityTypes.forEach(type => queryParameters.append('activityTypes', type));
    }
    if (filters.startTime) {
        queryParameters.append('startTime', filters.startTime);
    }
    if (filters.endTime) {
        queryParameters.append('endTime', filters.endTime);
    }
    if (filters.threshold) {
        queryParameters.append('threshold', filters.threshold);
    }

    const endpoint = `/heatmap/${type}/${processDefinitionId}?${queryParameters}`;
    console.log(`Fetching heatmap data from: ${endpoint}`);
    try {
        const response = await fetch(endpoint);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const data = await response.json();
        console.log('Received heatmap data:', data);
        return data.reduce((acc, cur) => {
            if (type === 'time') {
                acc[cur.activityId] = cur.averageTime;
            } else if (type === 'count') {
                acc[cur.activityId] = cur.count;
            } else if (type === 'error') {
                acc[cur.activityId] = cur.incidentCount;
            }
            return acc;
        }, {});
    } catch (error) {
        console.error(`Failed to fetch heatmap data for type ${type} with filters:`, error);
        return {};
    }
}

async function loadBpmnModelAndHeatmap(processDefinitionId, type, filters) {
    const container = document.getElementById('bpmnContainer');
    if (viewer) {
        viewer.destroy();
    }
    viewer = new BpmnJS({ container: '#bpmnContainer' });

    const url = `/process/bpmn/${processDefinitionId}`;
    console.log(`Загрузка BPMN модели с: ${url}`);

    const startTime = performance.now(); // Начало замера времени

    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        const bpmnXML = await response.text();
        await viewer.importXML(bpmnXML);
        console.log('BPMN diagram successfully displayed.');

        if (currentHeatmapOverlay) {
            currentHeatmapOverlay.destroy();
        }
        const heatmapData = await fetchHeatmapData(processDefinitionId, type, filters);
        if (Object.keys(heatmapData).length === 0) {
            console.warn('Данные тепловой карты пусты.');
            return;
        }
        currentHeatmapOverlay = new HeatmapOverlay(viewer, heatmapData, {
            noSequenceHighlight: false
        });
        var canvas = viewer.get('canvas');
        canvas.zoom('fit-viewport');
        console.log('Диаграмма BPMN успешно отображена.');
    } catch (err) {
        console.error('could not import BPMN 2.0 diagram', err);
    }
    const endTime = performance.now(); // Окончание замера времени
    console.log(`Время на отрисовку BPMN и тепловой карты: ${(endTime - startTime).toFixed(2)} мс`);
}

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('applyFilters').addEventListener('click', async function() {
        const processDefinitionId = document.getElementById('processId').value;
        // const processDefinitionId = 'orderConfirmation'; // Пример идентификатора процесса
        const selectedType = document.getElementById('heatmapType').value;
        const selectedActivityTypes = Array.from(document.querySelectorAll('input[name="activityType"]:checked')).map(el => el.value);
        const startTime = document.getElementById('startTime').value ? new Date(document.getElementById('startTime').value).toISOString() : undefined;
        const endTime = document.getElementById('endTime').value ? new Date(document.getElementById('endTime').value).toISOString() : undefined;
        const threshold = document.getElementById('threshold').value;
        const filters = {
            activityTypes: selectedActivityTypes,
            startTime: startTime,
            endTime: endTime,
            threshold: threshold
        };
        console.log('Applying filters:', filters);
        await loadBpmnModelAndHeatmap(processDefinitionId, selectedType, filters);
    });
});

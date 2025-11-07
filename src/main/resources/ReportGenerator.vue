<script setup lang="ts">
import {ref, reactive, watch, computed, nextTick} from 'vue';
import axios from 'axios';
import draggable from 'vuedraggable';

import {Button} from '@/components/ui/button';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {Checkbox} from '@/components/ui/checkbox';
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from '@/components/ui/accordion';
import {ResizableHandle, ResizablePanel, ResizablePanelGroup} from '@/components/ui/resizable';
import {ScrollArea} from '@/components/ui/scroll-area';
import {Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle} from '@/components/ui/dialog';
import Icon from '@/components/Icon.vue';
import {useToast} from 'primevue/usetoast';
import {Separator} from "@/components/ui/separator"
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue
} from "@/components/ui/select";
import {LoaderCircle} from "lucide-vue-next";
import { TabsContent, TabsList, TabsRoot, TabsTrigger } from 'reka-ui'

const toast = useToast();

const props = defineProps<{
  modelValue: boolean;
  activeFilters: ActiveFilters;
  userRole: 'admin' | 'doctor';
  reportType?: 'appointments' | 'doctors';
  config?: any;
  data?: any[];
}>();

const emit = defineEmits(['update:modelValue']);


const reportLoading = ref(false);
const pdfUrl = ref<string | null>(null);
let currentPdfBlobUrl: string | null = null;
const subreportEditorVisible = ref(false);
const editingSubreportField = ref('');

interface ColumnConfig {
  field: string;
  header: string;
  visible: boolean;
  width: number | null | undefined;
  format: string | null | undefined;
  groupCalculation: string;
}

interface ColorSettings {
  titleBackgroundColor: string;
  titleFontColor: string;
  columnHeaderBackgroundColor: string;
  columnHeaderFontColor: string;
  detailBackgroundColor: string;
  detailFontColor: string;
  groupHeaderBackgroundColor: string;
  groupHeaderFontColor: string;
  borderColor: string;
}

interface CompanyInfo {
  name: string;
  address: string;
  postalCode: string;
  city: string;
  taxId: string;
}

interface FormattingOptions {
  zebraStripes: boolean;
  generateBookmarks: boolean;
  highlightRules: Array<{
    field: string;
    operator: string;
    value: string;
    color: string;
    id: string;
  }>;
}

interface ActiveFilters {
  search?: string;
  specialization?: string;
}

interface ReportConfig {
  title: string;
  orientation: 'PORTRAIT' | 'LANDSCAPE';
  pageFormat: 'A4' | 'A3' | 'LETTER' | 'LEGAL';
  theme: 'DEFAULT' | 'CLASSIC' | 'MODERN' | 'CORPORATE' | 'MINIMAL';
  companyInfo: CompanyInfo | null;
  footerLeftText: string | null;
  columns: ColumnConfig[];
  groups: Array<{ field: string; showFooter: boolean }>;
  pageFooterEnabled: boolean;
  summaryBandEnabled: boolean;
  formattingOptions: FormattingOptions;
  subreportConfigs: {
    [key: string]: Omit<ReportConfig, 'subreportConfigs'>;
  };
  colorSettings: ColorSettings;
}

const availableFields = computed(() => {
  if (props.reportType === 'doctors') {
    return [
      {field: 'id', header: 'ID', type: 'numeric'},
      {field: 'first_name', header: 'Imię', type: 'text'},
      {field: 'last_name', header: 'Nazwisko', type: 'text'},
      {field: 'specialization', header: 'Specjalizacja', type: 'text'},
      {field: 'user_email', header: 'Email', type: 'text'},
      {field: 'bio', header: 'Bio', type: 'text'},
      {field: 'created_at', header: 'Data dodania', type: 'date'},
      {field: 'procedures', header: '', type: 'subreport'},
    ];
  } else {
    return [
      {field: 'id', header: 'ID Wizyty', type: 'numeric'},
      {field: 'appointment_datetime', header: 'Data wizyty', type: 'date'},
      {field: 'status_translated', header: 'Status', type: 'text'},
      {field: 'patient_name', header: 'Pacjent', type: 'text'},
      {field: 'doctor_full_name', header: 'Lekarz', type: 'text'},
      {field: 'procedure_name', header: 'Zabieg', type: 'text'},
      {field: 'procedure_base_price', header: 'Cena (PLN)', type: 'numeric'},
      {field: 'patient_notes', header: 'Notatki pacjenta', type: 'text'},
    ];
  }
});

const avaliablePageFormat = computed(() => {
  return ['A4', 'A3', 'LETTER', 'LEGAL'];
});

const reportConfig = reactive<ReportConfig>({
  title: 'Zestawienie Wizyt',
  orientation: 'PORTRAIT',
  pageFormat: 'A4',
  theme: 'CLASSIC',
  companyInfo: {
    name: 'Softres',
    address: 'ul. Zaciszna 44',
    postalCode: '00-001',
    city: 'Rzeszów',
    taxId: '123-456-78-90'
  },
  footerLeftText: 'eBudżet - ZSI "Sprawny Urząd" \nBUK Softres - www.softres.pl',
  columns: [],
  groups: [] as Array<{ field: string; showFooter: boolean }>,
  pageFooterEnabled: true,
  summaryBandEnabled: false,
  formattingOptions: {
    zebraStripes: false,
    generateBookmarks: false,
    highlightRules: []
  },
  subreportConfigs: {
  },
  colorSettings: {
    titleBackgroundColor: '#2A3F54',
    titleFontColor: '#FFFFFF',
    columnHeaderBackgroundColor: '#C6D8E4',
    columnHeaderFontColor: '#000000',
    detailBackgroundColor: '#FFFFFF',
    detailFontColor: '#000000',
    groupHeaderBackgroundColor: '#D0D8E0',
    groupHeaderFontColor: '#000000',
    borderColor: '#CCCCCC',
  },
});

const isSubreportColumn = (fieldDame: string): boolean => {
  const fieldDef = availableFields.value.find(f => f.field === fieldDame);
  return fieldDef ? fieldDef.type === 'subreport' : false;
};

const initializeConfig = () => {
  if (props.config) {
    Object.assign(reportConfig, props.config);
  } else {
    reportConfig.title = props.reportType === 'doctors' ? 'Raport Lekarzy' : 'Zestawienie Wizyt';

    if (props.reportType === 'doctors') {
      reportConfig.formattingOptions.highlightRules = [
        {
          field: 'specialization',
          operator: 'EQUALS',
          value: 'Chirurg Plastyczny',
          color: '#FFFFFF',
          id: `rule-${Date.now()}`
        }
      ];

      reportConfig.subreportConfigs.procedures = {
        title: 'Procedury',
        theme: reportConfig.theme,
        orientation: 'PORTRAIT',
        companyInfo: null,
        pageFormat: "A4",
        footerLeftText: null,
        columns: [
          {
            field: 'item',
            header: 'Nazwa procedury',
            visible: true,
            width: -1,
            format: null,
            groupCalculation: 'NONE'
          },
          {
            field: 'category',
            header: 'Kategoria',
            visible: true,
            width: -1,
            format: null,
            groupCalculation: 'NONE'
          },
          {
            field: 'price',
            header: 'Cena bazowa (PLN)',
            visible: true,
            width: -1,
            format: '#,##0.00',
            groupCalculation: 'SUM'
          }
        ],
        groups: [],
        pageFooterEnabled: false,
        summaryBandEnabled: false,
        formattingOptions: {zebraStripes: true, generateBookmarks: false, highlightRules: []},
        colorSettings: {
          titleBackgroundColor: '#2A3F54',
          titleFontColor: '#FFFFFF',
          columnHeaderBackgroundColor: '#C6D8E4',
          columnHeaderFontColor: '#000000',
          detailBackgroundColor: '#FFFFFF',
          detailFontColor: '#000000',
          groupHeaderBackgroundColor: '#D0D8E0',
          groupHeaderFontColor: '#000000',
          borderColor: '#CCCCCC',
        },
      };
    } else {
      reportConfig.formattingOptions.highlightRules = [
        {
          field: 'status_translated',
          operator: 'EQUALS',
          value: 'Anulowana',
          color: '#FFF0F0',
          id: `rule-${Date.now()}`
        }
      ];
    }
  }

  reportConfig.columns = availableFields.value.map(f => ({
    field: f.field,
    header: f.header,
    visible: !['id', 'patient_notes', 'billing_details', 'bio'].includes(f.field),
    width: -1,
    format: f.type === 'numeric' ? '#,##0.00' : (f.type === 'date' ? 'yyyy-MM-dd HH:mm' : null),
    groupCalculation: f.type === 'numeric' ? 'SUM' : 'NONE',
  }));
};

const highlightOperators = [
  {value: 'EQUALS', label: 'równa się'},
  {value: 'NOT_EQUALS', label: 'nie równa się'},
  {value: 'CONTAINS', label: 'zawiera'},
  {value: 'GREATER_THAN', label: 'większe niż'},
  {value: 'LESS_THAN', label: 'mniejsze niż'},
]

const isLandscape = computed({
  get: () => reportConfig.orientation === 'LANDSCAPE',
  set: (val) => {
    reportConfig.orientation = val ? 'LANDSCAPE' : 'PORTRAIT';
  }
});

const addGroup = () => {
  if (availableFields.value.length > 0) {
    const defaultField = availableFields.value[0];
    reportConfig.groups.push({
      field: defaultField.field,
      showFooter: false
    });
  }
};

const removeGroup = (index: number) => {
  reportConfig.groups.splice(index, 1);
};

function normalizeColor(color: string): string {
  if (!color) return '#FFFFFF';
  if (color.startsWith('#')) color = color.slice(1);
  if (color.length === 3) {
    color = color.split('').map(c => c + c).join('');
  }
  if (color.length !== 6) return '#FFFFFF';
  return `#${color.toUpperCase()}`;
}

const configureSubreport = (fieldName: string) => {
  if (!reportConfig.subreportConfigs[fieldName]) {
    reportConfig.subreportConfigs[fieldName] = {
      title: 'Podraport',
      theme: reportConfig.theme,
      orientation: 'PORTRAIT',
      companyInfo: null,
      pageFormat: 'A4',
      footerLeftText: null,
      columns: [
        {field: 'item', header: 'Pozycja', visible: true, width: -1, format: null, groupCalculation: 'NONE'},
        {field: 'quantity', header: 'Ilość', visible: true, width: 80, format: null, groupCalculation: 'NONE'},
        {field: 'price', header: 'Cena', visible: true, width: 100, format: '#,##0.00', groupCalculation: 'SUM'}
      ],
      groups: [],
      pageFooterEnabled: false,
      summaryBandEnabled: false,
      formattingOptions: {zebraStripes: true, generateBookmarks: false, highlightRules: []},
      colorSettings: {
        titleBackgroundColor: '#2A3F54',
        titleFontColor: '#FFFFFF',
        columnHeaderBackgroundColor: '#C6D8E4',
        columnHeaderFontColor: '#000000',
        detailBackgroundColor: '#FFFFFF',
        detailFontColor: '#000000',
        groupHeaderBackgroundColor: '#D0D8E0',
        groupHeaderFontColor: '#000000',
        borderColor: '#CCCCCC',
      },
    };
  }

  editingSubreportField.value = fieldName;
  subreportEditorVisible.value = true;
};

const availableThemes = [
  {value: 'CLASSIC', label: 'Klasyczny'},
  {value: 'MODERN', label: 'Nowoczesny'},
  {value: 'CORPORATE', label: 'Korporacyjny'},
  {value: 'MINIMAL', label: 'Minimalistyczny'}
];

const apiEndpoint = computed(() => {
  const rolePrefix = props.userRole === 'doctor' ? 'doctor' : 'admin';

  if (props.reportType === 'doctors') {
    return `/api/v1/admin/doctors/report`;
  }
  else if (props.reportType === 'appointments') {
    return `/api/v1/${rolePrefix}/appointments/report`;
  }
  return `/api/v1/admin/appointments/report`;
});

const refreshPreview = async () => {
  reportLoading.value = true;
  if (currentPdfBlobUrl) {
    URL.revokeObjectURL(currentPdfBlobUrl);
  }

  try {
    const payloadConfig = JSON.parse(JSON.stringify(reportConfig));

    if (payloadConfig.subreportConfigs) {
      for (const key in payloadConfig.subreportConfigs) {
        delete payloadConfig.subreportConfigs[key].subreportConfigs;
      }
    }
    if (!payloadConfig.subreportConfigs || Array.isArray(payloadConfig.subreportConfigs)) {
      payloadConfig.subreportConfigs = {};
    }

    if (payloadConfig.groups) {
      payloadConfig.groups = payloadConfig.groups
          .filter((g: { field: string }) => g.field)
          .map((g: { field: string; showFooter: boolean }) => {
            const fieldDef = availableFields.value.find(f => f.field === g.field);
            const header = fieldDef ? fieldDef.header : g.field;
            return {...g, label: `"${header}: " + $F{${g.field}}`};
          });
    }

    if (payloadConfig.formattingOptions && payloadConfig.formattingOptions.highlightRules) {
      payloadConfig.formattingOptions.highlightRules.forEach((rule: any) => {
        rule.color = normalizeColor(rule.color);
      });
    }
    if (payloadConfig.colorSettings) {
      for (const key in payloadConfig.colorSettings) {
        if (Object.prototype.hasOwnProperty.call(payloadConfig.colorSettings, key) && payloadConfig.colorSettings[key]) {
          payloadConfig.colorSettings[key] = normalizeColor(payloadConfig.colorSettings[key]);
        }
      }
    }
    if (payloadConfig.subreportConfigs) {
      for (const reportKey in payloadConfig.subreportConfigs) {
        const subreport = payloadConfig.subreportConfigs[reportKey];
        if (subreport.colorSettings) {
          for (const key in subreport.colorSettings) {
            subreport.colorSettings[key] = normalizeColor(subreport.colorSettings[key]);
          }
        }
      }
    }

    let response;

    if (props.reportType === 'doctors') {
      const params = new URLSearchParams();
      if (props.activeFilters.search) params.append('search', props.activeFilters.search);
      if (props.activeFilters.specialization) params.append('specialization', props.activeFilters.specialization);

      params.append('config', JSON.stringify(payloadConfig));

      response = await axios.get(`${apiEndpoint.value}?${params.toString()}`, {
        responseType: 'blob'
      });
    } else {
      response = await axios.post(apiEndpoint.value, {
        config: payloadConfig,
        filters: props.activeFilters
      }, {responseType: 'blob'});
    }

    const blob = new Blob([response.data], {type: 'application/pdf'});
    currentPdfBlobUrl = URL.createObjectURL(blob);
    pdfUrl.value = currentPdfBlobUrl;
    toast.add({severity: 'success', summary: 'Sukces', detail: 'Podgląd odświeżony.', life: 2000});
  } catch (error) {
    console.error('Błąd podglądu:', error);
    let errorText = 'Wystąpił nieznany błąd.';
    if ((error as any).response?.data) {
      try {
        const errorData = (error as any).response.data;
        if (errorData instanceof Blob) {
          const text = await errorData.text();
          try {
            const jsonError = JSON.parse(text);
            errorText = jsonError.details || jsonError.error || text;
          } catch {
            errorText = text;
          }
        } else if (typeof errorData === 'object') {
          errorText = errorData.details || errorData.error || JSON.stringify(errorData);
        } else {
          errorText = errorData.toString();
        }
      } catch (e) {
        console.error('Nie udało się sparsować odpowiedzi błędu:', e);
      }
    }
    toast.add({severity: 'error', summary: 'Błąd generowania raportu', detail: errorText, life: 7000});
    pdfUrl.value = null;
  } finally {
    reportLoading.value = false;
  }
};

const downloadReport = () => {
  if (!pdfUrl.value) {
    toast.add({severity: 'warn', summary: 'Brak podglądu', detail: 'Najpierw odśwież podgląd.', life: 3000});
    return;
  }
  const link = document.createElement('a');
  link.href = pdfUrl.value;
  link.setAttribute('download', `${reportConfig.title.replace(/\s/g, '_')}.pdf`);
  document.body.appendChild(link);
  link.click();
  link.remove();
};

const addHighlightRule = () => {
  const defaultField = props.reportType === 'doctors' ? 'specialization' : 'procedure_base_price';
  const defaultValue = props.reportType === 'doctors' ? 'Chirurg Plastyczny' : '1000';
  const defaultOperator = props.reportType === 'doctors' ? 'EQUALS' : 'GREATER_THAN';

  reportConfig.formattingOptions.highlightRules.push({
    field: defaultField,
    operator: defaultOperator,
    value: defaultValue,
    color: '#FFF0F0',
    id: `rule-${Date.now()}`
  });
};

const removeHighlightRule = (index: number) => {
  reportConfig.formattingOptions.highlightRules.splice(index, 1);
};

watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    initializeConfig();
    nextTick(() => {
      if (reportConfig.columns.length === 0) {
        reportConfig.columns = availableFields.value.map(f => ({
          field: f.field,
          header: f.header,
          visible: !['id', 'patient_notes', 'billing_details', 'bio'].includes(f.field),
          width: -1,
          format: f.type === 'numeric' ? '#,##0.00' : (f.type === 'date' ? 'yyyy-MM-dd HH:mm' : null),
          groupCalculation: f.type === 'numeric' ? 'SUM' : 'NONE',
        }));
      }
      refreshPreview();
    });
  }
});

watch(() => props.reportType, () => {
  initializeConfig();
  nextTick(() => {
    if (reportConfig.columns.length === 0) {
      reportConfig.columns = availableFields.value.map(f => ({
        field: f.field,
        header: f.header,
        visible: !['id', 'patient_notes', 'billing_details', 'bio'].includes(f.field),
        width: -1,
        format: f.type === 'numeric' ? '#,##0.00' : (f.type === 'date' ? 'yyyy-MM-dd HH:mm' : null),
        groupCalculation: f.type === 'numeric' ? 'SUM' : 'NONE',
      }));
    }
  });
});

watch(() => reportConfig.pageFormat, (newPageFormat) => {
  if (reportConfig.subreportConfigs) {

    for (const key in reportConfig.subreportConfigs) {
      const subreport = reportConfig.subreportConfigs[key];
      if (subreport) {
        subreport.pageFormat = newPageFormat;
      }
    }
  }
});


watch(() => reportConfig.colorSettings, () => {
}, { deep: true });

const customColors = [
  { key: 'titleBackgroundColor', label: 'Tło tytułu' },
  { key: 'titleFontColor', label: 'Tekst tytułu' },
  { key: 'columnHeaderBackgroundColor', label: 'Tło nagłówków' },
  { key: 'columnHeaderFontColor', label: 'Tekst nagłówków' },
  { key: 'detailBackgroundColor', label: 'Tło szczegółów' },
  { key: 'detailFontColor', label: 'Tekst szczegółów' },
  { key: 'groupHeaderBackgroundColor', label: 'Tło grupy' },
  { key: 'groupHeaderFontColor', label: 'Tekst grupy' },
  { key: 'borderColor', label: 'Obramowanie' },
] as const;
</script>

<template>
  <Dialog :open="props.modelValue" @update:open="emit('update:modelValue', $event)">
    <DialogContent class="w-[95vw] sm:w-[90vw] lg:w-[90vw] xl:w-[90vw] max-w-none h-[95vh] sm:h-[90vh] flex flex-col dark:bg-background  bg-background border-nova-primary/20">
      <DialogHeader class="border-b border-nova-primary/10">
        <DialogTitle class="text-md sm:text-md text-nova-dark font-semibold">Generator Raportów</DialogTitle>
        <DialogDescription class="text-xs sm:text-sm mb-1 text-muted-foreground">
          Dostosuj raport i zobacz podgląd na żywo. Raport jest generowany na podstawie aktualnie ustawionych filtrów.
        </DialogDescription>
      </DialogHeader>

      <div class="flex-grow min-h-0">
        <ResizablePanelGroup direction="horizontal" class="h-full dark:bg-background bg-background w-full">
          <ResizablePanel :default-size="50" :min-size="40" class="hidden lg:block border-r dark:border-nova-primary/10 border-nova-primary/10">
            <div class="flex flex-col h-full sm:p-1 pr-1 ">
              <ScrollArea class="h-full overflow-y-auto pr-1">
                <Accordion type="single" collapsible class="w-full" default-value="item-1">
                  <AccordionItem value="item-1" class="dark:border-nova-primary/10 border-nova-primary/20">
                    <AccordionTrigger class="text-base sm:text-lg text-nova-dark hover:text-nova-primary dark:hover:nova-accent dark:text-nova-accent">Opcje Główne</AccordionTrigger>
                    <AccordionContent class="pt-0">
                      <ScrollArea class="max-h-64 overflow-y-auto">
                        <div class="grid grid-cols-1 lg:grid-cols-4 items-center gap-2 sm:gap-4">
                          <Label for="report-title" class="lg:text-right text-nova-dark font-medium">Tytuł Raportu</Label>
                          <Input id="report-title" v-model="reportConfig.title"
                                 class="lg:col-span-3 border-nova-primary/30 focus:border-nova-accent focus:ring-nova-accent/20"/>
                        </div>
                        <div class="grid grid-cols-1 lg:grid-cols-4 mt-2 items-center gap-2 sm:gap-4">
                          <Label for="report-format" class="lg:text-right text-nova-dark font-medium">Format raportu</Label>
                          <Select v-model="reportConfig.pageFormat">
                            <SelectTrigger class="lg:col-span-3">
                              <SelectValue placeholder="Wybierz format..." />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem v-for="format in avaliablePageFormat" :key="format" :value="format">
                                {{ format }}
                              </SelectItem>
                            </SelectContent>
                          </Select>
                        </div>

                        <div class="grid grid-cols-1 lg:grid-cols-4 mt-2 mb-2 items-center gap-2 sm:gap-4">
                          <Label for="orientation-checkbox" class="lg:text-right text-nova-dark font-medium">Orientacja</Label>
                          <div class="lg:col-span-3 flex items-center space-x-2">
                            <Checkbox id="orientation-checkbox"
                                      class="data-[state=checked]:bg-nova-accent data-[state=unchecked]:bg-background border-nova-primary/40 data-[state=checked]:border-nova-accent"
                                      v-model:checked="isLandscape"/>
                            <Label for="orientation-checkbox" class="text-nova-dark">Pozioma</Label>
                          </div>
                        </div>
                        <div class="grid grid-cols-1 lg:grid-cols-4 mt-2 mb-2 items-center gap-2 sm:gap-4">
                          <Label class="lg:text-right text-nova-dark font-medium">Stopka</Label>
                          <div class="flex items-center space-x-2 lg:col-span-3">
                            <Checkbox id="page-footer"
                                      class="data-[state=checked]:bg-nova-accent data-[state=unchecked]:bg-background border-nova-primary/40 data-[state=checked]:border-nova-accent"
                                      v-model:checked="reportConfig.pageFooterEnabled"/>
                            <label for="page-footer" class="text-sm font-medium leading-none text-nova-dark">Dołącz stopkę</label>
                          </div>
                        </div>
                        <div class="grid grid-cols-1 lg:grid-cols-4 mt-2 mb-2 items-center gap-2 sm:gap-4">
                          <Label class="lg:text-right text-nova-dark font-medium">Podsumowanie</Label>
                          <div class="flex items-center space-x-2 lg:col-span-3">
                            <Checkbox id="summary-band"
                                      class="data-[state=checked]:bg-nova-accent data-[state=unchecked]:bg-background border-nova-primary/40 data-[state=checked]:border-nova-accent"
                                      v-model:checked="reportConfig.summaryBandEnabled"/>
                            <label for="summary-band" class="text-sm font-medium leading-none text-nova-dark">Dołącz podsumowanie na końcu</label>
                          </div>
                        </div>
                        <div v-if="reportConfig.subreportConfigs.procedures" class="grid grid-cols-1 lg:grid-cols-4 mt-2 items-center gap-2 sm:gap-4">
                          <Label class="lg:text-left text-nova-dark font-medium">Podsumowanie dla podraportu</Label>
                          <div class="flex items-center space-x-2 lg:col-span-3">
                            <Checkbox id="subreport-summary"
                                      class="data-[state=checked]:bg-nova-accent data-[state=unchecked]:bg-background border-nova-primary/40 data-[state=checked]:border-nova-accent"
                                      v-model:checked="reportConfig.subreportConfigs.procedures.summaryBandEnabled"/>
                            <label for="subreport-summary" class="text-sm font-medium leading-none text-nova-dark">Pokaż podsumowanie podraportu</label>
                          </div>
                        </div>

                        <Separator class="my-4 bg-nova-primary/20"/>

                        <div v-if="reportConfig.companyInfo">
                          <div class="grid grid-cols-1 lg:grid-cols-4 mt-2 items-start gap-2 sm:gap-4">
                            <Label for="company-info" class="lg:text-left pt-2 text-nova-dark font-medium">Nazwa jednostki</Label>
                            <Input id="company-info" v-model="reportConfig.companyInfo.name"
                                   class="lg:col-span-3 w-full border-nova-primary/30 focus:border-nova-accent focus:ring-nova-accent/20"/>
                          </div>
                          <div class="grid grid-cols-1 lg:grid-cols-4 mt-2 items-start gap-2 sm:gap-4">
                            <Label for="company-address" class="lg:text-left pt-2 text-nova-dark font-medium">Adres jednostki</Label>
                            <Input id="company-address" v-model="reportConfig.companyInfo.address"
                                   class="lg:col-span-3 w-full border-nova-primary/30 focus:border-nova-accent focus:ring-nova-accent/20"/>
                          </div>
                          <div class="grid grid-cols-1 lg:grid-cols-4 mt-2 items-start gap-2 sm:gap-4">
                            <Label for="company-postalcode" class="lg:text-left pt-2 text-nova-dark font-medium">Kod pocztowy</Label>
                            <Input id="company-postalcode" v-model="reportConfig.companyInfo.postalCode"
                                   class="lg:col-span-3 w-full border-nova-primary/30 focus:border-nova-accent focus:ring-nova-accent/20"/>
                          </div>
                          <div class="grid grid-cols-1 lg:grid-cols-4 mt-2 items-start gap-2 sm:gap-4">
                            <Label for="company-city" class="lg:text-left pt-2 text-nova-dark font-medium">Miasto</Label>
                            <Input id="company-city" v-model="reportConfig.companyInfo.city"
                                   class="lg:col-span-3 w-full border-nova-primary/30 focus:border-nova-accent focus:ring-nova-accent/20"/>
                          </div>
                          <div class="grid grid-cols-1 lg:grid-cols-4 mt-2 items-start gap-2 sm:gap-4">
                            <Label for="company-taxid" class="lg:text-left pt-2 text-nova-dark font-medium">NIP</Label>
                            <Input id="company-taxid" v-model="reportConfig.companyInfo.taxId"
                                   class="lg:col-span-3 w-full border-nova-primary/30 focus:border-nova-accent focus:ring-nova-accent/20"/>
                          </div>
                        </div>

                        <Separator class="my-4 bg-nova-primary/20"/>

                        <div v-if="reportConfig.pageFooterEnabled" class="grid grid-cols-1 lg:grid-cols-4 mt-2 items-start gap-2 sm:gap-4">
                          <Label for="footer-text" class="lg:text-left pt-2 text-nova-dark font-medium">Tekst w stopce</Label>
                          <textarea id="footer-text" v-model="reportConfig.footerLeftText"
                                    class="lg:col-span-3 w-full rounded-md border border-nova-primary/30 bg-background px-3 py-2 text-sm focus:border-nova-accent focus:ring-2 focus:ring-nova-accent/20 focus:outline-none"
                                    rows="2"></textarea>
                        </div>
                      </ScrollArea>
                    </AccordionContent>
                  </AccordionItem>
                  <AccordionItem value="item-2" class="border-nova-primary/20">
                    <AccordionTrigger class="text-base sm:text-lg text-nova-dark hover:text-nova-primary">Kolumny</AccordionTrigger>
                    <AccordionContent class="pt-0">
                      <ScrollArea class="max-h-66 overflow-y-auto">
                        <draggable v-model="reportConfig.columns" item-key="field"
                                   handle=".drag-handle" ghost-class="ghost-class">
                          <template #item="{ element: col, index }">
                            <div class="grid grid-cols-3 sm:grid-cols-6 gap-x-1 gap-y-2 items-center mt-2 p-2 rounded-md border border-nova-primary/10 bg-nova-light/30"
                                 :class="{ 'bg-nova-accent/10 border-nova-accent/30': availableFields.find(f => f.field === col.field)?.type === 'subreport' }">
                              <div class="drag-handle cursor-move p-2 text-nova-primary hover:text-nova-accent">
                                <Icon name="grip" size="18"/>
                              </div>
                              <Checkbox v-model:checked="col.visible"
                                        class="data-[state=checked]:bg-nova-accent data-[state=unchecked]:bg-background border-nova-primary/40 data-[state=checked]:border-nova-accent"/>
                              <div class="col-span-1 sm:col-span-2 flex items-center space-x-2">
                                <Input v-if="!isSubreportColumn(col.field)" v-model="col.header"
                                       class="flex-1 text-xs h-8 border-nova-primary/30 focus:border-nova-accent"/>
                                <Button v-if="availableFields.find(f => f.field === col.field)?.type === 'subreport' && col.visible"
                                        @click="configureSubreport(col.field)"
                                        size="sm"
                                        variant="outline"
                                        class="h-8 px-2 text-xs w-full border-nova-accent text-nova-accent hover:bg-nova-accent hover:text-white"
                                >
                                  <Icon name="settings" size="12" class="mr-1"/>
                                  <span class="hidden sm:inline">Konfiguruj podraport</span>
                                </Button>
                              </div>
                              <Input v-model.number="col.width" type="number"
                                     class="text-xs h-8 text-center w-full border-nova-primary/30 focus:border-nova-accent" placeholder="auto"
                                     :disabled="availableFields.find(f => f.field === col.field)?.type === 'subreport'"/>
                              <Input v-model="col.format" class="text-xs h-8 w-full border-nova-primary/30 focus:border-nova-accent"
                                     placeholder="np. #,##0.00"
                                     :disabled="availableFields.find(f => f.field === col.field)?.type === 'subreport'"/>
                            </div>
                          </template>
                        </draggable>
                      </ScrollArea>
                    </AccordionContent>
                  </AccordionItem>
                  <AccordionItem value="item-3" class="border-nova-primary/20">
                    <AccordionTrigger class="text-base sm:text-lg text-nova-dark hover:text-nova-primary">Grupowanie</AccordionTrigger>
                    <AccordionContent class="pt-0">
                      <ScrollArea class="max-h-64 overflow-y-auto">
                        <draggable v-model="reportConfig.groups" item-key="index"
                                   handle=".drag-handle" ghost-class="ghost-class">
                          <template #item="{ element: group, index }">
                            <div class="p-3 border border-nova-primary/20 rounded-md mb-2 bg-nova-light/50 flex flex-col sm:flex-row items-start space-y-2 sm:space-y-0 sm:space-x-2">
                              <div class="drag-handle cursor-move p-2 text-nova-primary hover:text-nova-accent">
                                <Icon name="grip" size="18"/>
                              </div>
                              <div class="flex-grow w-full">
                                <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-4">
                                  <div>
                                    <Label :for="`group-field-${index}`" class="text-nova-dark font-medium">Grupuj po polu</Label>
                                    <Select v-model="group.field">
                                      <SelectTrigger :id="`group-field-${index}`">
                                        <SelectValue placeholder="Wybierz pole..."/>
                                      </SelectTrigger>
                                      <SelectContent>
                                        <SelectItem v-for="field in availableFields" :key="field.field" :value="field.field">
                                          {{ field.header }}
                                        </SelectItem>
                                      </SelectContent>
                                    </Select>
                                  </div>
                                </div>
                                <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center mt-2 space-y-2 sm:space-y-0 sm:space-x-2">
                                  <div class="flex items-center space-x-2">
                                    <Checkbox :id="`group-footer-${index}`"
                                              class="data-[state=checked]:bg-nova-accent data-[state=unchecked]:bg-background border-nova-primary/40 data-[state=checked]:border-nova-accent"
                                              v-model:checked="group.showFooter"/>
                                    <label :for="`group-footer-${index}`" class="text-sm text-nova-dark">Pokaż podsumowanie</label>
                                  </div>
                                  <Button variant="destructive" size="sm" @click="removeGroup(index)"
                                          class="bg-red-500 hover:bg-red-600 text-white">Usuń</Button>
                                </div>
                              </div>
                            </div>
                          </template>
                        </draggable>
                        <Button @click="addGroup" class="mt-2 w-full border-nova-accent text-nova-accent hover:bg-nova-accent hover:text-white" variant="outline">
                          <Icon name="plus" class="mr-2" size="8"/>
                          Dodaj nową grupę
                        </Button>
                      </ScrollArea>
                    </AccordionContent>
                  </AccordionItem>
                  <AccordionItem value="item-4" class="border-nova-primary/20">
                    <AccordionTrigger class="text-base sm:text-lg text-nova-dark hover:text-nova-primary">Formatowanie</AccordionTrigger>
                    <AccordionContent class="space-y-4 pt-0">
                      <ScrollArea class="max-h-54 overflow-y-auto">
                        <div class="flex flex-col sm:flex-row items-start sm:items-center space-y-4 sm:space-y-0 sm:space-x-6">
                          <div class="flex items-center space-x-2">
                            <Checkbox id="zebra-stripes"
                                      class="data-[state=checked]:bg-nova-accent data-[state=unchecked]:bg-background border-nova-primary/40 data-[state=checked]:border-nova-accent"
                                      v-model:checked="reportConfig.formattingOptions.zebraStripes"/>
                            <label for="zebra-stripes" class="text-sm font-medium text-nova-dark">Paski zebry</label>
                          </div>

                        </div>
                        <Separator class="my-4 bg-nova-primary/20"/>
                        <TabsRoot default-value="highlighting" class="w-full">
                          <TabsList class="grid w-full grid-cols-2">
                            <TabsTrigger value="highlighting" class="transition duration-300 ease-in-out hover:-translate-y-1">
                              Podświetlanie
                            </TabsTrigger>
                            <TabsTrigger value="colors" class="transition duration-300 ease-in-out hover:-translate-y-1">
                              Kolory
                            </TabsTrigger>
                          </TabsList>
                          <TabsContent value="highlighting" class="mt-4">
                            <Label class="font-semibold text-nova-dark">Reguły podświetlania wierszy</Label>
                            <div v-for="(rule, index) in reportConfig.formattingOptions.highlightRules" :key="rule.id"
                                 class="p-3 border border-nova-primary/20 rounded-md mt-2 space-y-2 bg-nova-light/30">
                              <div class="flex flex-col sm:flex-row items-start sm:items-center space-y-2 sm:space-y-0 sm:space-x-2">
                                <span class="text-sm font-bold text-nova-primary">JEŚLI</span>
                                <Select v-model="rule.field">
                                  <SelectTrigger>
                                    <SelectValue placeholder="Wybierz pole..."/>
                                  </SelectTrigger>
                                  <SelectContent>
                                    <SelectItem v-for="field in availableFields" :key="field.field" :value="field.field">{{ field.header }}</SelectItem>
                                  </SelectContent>
                                </Select>
                                <Select v-model="rule.operator">
                                  <SelectTrigger>
                                    <SelectValue placeholder="Wybierz operator..."/>
                                  </SelectTrigger>
                                  <SelectContent>
                                    <SelectItem v-for="op in highlightOperators" :key="op.value" :value="op.value">{{ op.label }}</SelectItem>
                                  </SelectContent>
                                </Select>
                                <template v-if="(availableFields.find(f => f.field === rule.field)?.type === 'numeric') && rule.operator !== 'CONTAINS'">
                                  <Input v-model="(rule as any).value" type="number" placeholder="Wartość liczbowa"
                                         class="flex-1 w-full sm:w-auto text-sm h-8 border-nova-primary/30 focus:border-nova-accent"/>
                                </template>
                                <template v-else>
                                  <Input v-model="(rule as any).value" placeholder="Wartość"
                                         class="flex-1 w-full sm:w-auto text-sm h-8 border-nova-primary/30 focus:border-nova-accent"/>
                                </template>
                              </div>
                              <div class="flex flex-col sm:flex-row items-start sm:items-center space-y-2 sm:space-y-0 sm:space-x-2">
                                <span class="text-sm font-bold text-nova-primary">WTEDY</span>
                                <Label for="color-picker" class="text-sm whitespace-nowrap text-nova-dark">kolor tła:</Label>
                                <Input v-model="rule.color" type="color" id="color-picker" class="h-8 w-12 p-1 border-nova-primary/30"/>
                                <div class="flex-grow"></div>
                                <Button variant="destructive" size="icon" @click="removeHighlightRule(index)"
                                        class="bg-red-500 hover:bg-red-600 text-white">
                                  <Icon name="trash" size="14"/>
                                </Button>
                              </div>
                            </div>
                            <Button @click="addHighlightRule" class="mt-2 w-full border-nova-accent text-nova-accent hover:bg-nova-accent hover:text-white" variant="outline">
                              <Icon name="plus" class="mr-2" size="8"/>
                              Dodaj regułę podświetlania
                            </Button>
                          </TabsContent>
                          <TabsContent value="colors" class="mt-4">
                            <Label class="font-semibold text-nova-dark">Niestandardowe kolory</Label>
                            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 gap-x-2 sm:gap-x-6 gap-y-2 mt-2">
                              <div v-for="color in customColors" :key="color.key" class="grid grid-cols-2 items-center gap-2">
                                <Label class="text-xs sm:text-sm text-left sm:text-right text-nova-dark">{{ color.label }}</Label>
                                <input v-model="reportConfig.colorSettings[color.key]" type="color"
                                       class="h-8 w-12 p-1 border border-nova-primary/30 rounded" />
                              </div>
                            </div>
                          </TabsContent>
                        </TabsRoot>
                      </ScrollArea>
                    </AccordionContent>
                  </AccordionItem>
                </Accordion>
              </ScrollArea>
            </div>
          </ResizablePanel>


          <div class="lg:hidden flex flex-col h-full w-full">
            <div class="flex flex-col flex-shrink-0 border-b border-nova-primary/20" style="max-height: 60vh;">
              <div class="p-2 sm:p-4">
                <h3 class="text-lg font-semibold text-nova-dark">Konfiguracja Raportu</h3>
              </div>
              <ScrollArea class="flex-grow p-2 sm:p-4 pt-0">
                <Accordion type="single" collapsible class="w-full" default-value="item-1">
                  <AccordionItem value="item-1" class="border-nova-primary/20">
                    <AccordionTrigger class="text-base text-nova-dark hover:text-nova-primary">Opcje Główne</AccordionTrigger>
                    <AccordionContent class="pt-4 space-y-4">
                      <div><Label for="mobile-report-title" class="text-nova-dark font-medium">Tytuł Raportu</Label><Input id="mobile-report-title" v-model="reportConfig.title" class="mt-1 border-nova-primary/30"/></div>
                      <div>
                        <Label for="mobile-report-format" class="text-nova-dark font-medium">Format</Label>
                        <Select id="mobile-report-format" v-model="reportConfig.pageFormat">
                          <SelectTrigger class="mt-1 w-full">
                            <SelectValue placeholder="Wybierz format..."/>
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem v-for="format in avaliablePageFormat" :key="format" :value="format">{{ format }}</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div class="space-y-2 pt-2">
                        <div class="flex items-center space-x-2"><Checkbox id="mobile-orientation" v-model:checked="isLandscape"/><Label for="mobile-orientation" class="text-nova-dark">Orientacja pozioma</Label></div>
                        <div class="flex items-center space-x-2"><Checkbox id="mobile-footer" v-model:checked="reportConfig.pageFooterEnabled"/><Label for="mobile-footer" class="text-nova-dark">Dołącz stopkę</Label></div>
                        <div class="flex items-center space-x-2"><Checkbox id="mobile-summary" v-model:checked="reportConfig.summaryBandEnabled"/><Label for="mobile-summary" class="text-nova-dark">Dołącz podsumowanie</Label></div>
                        <div v-if="reportConfig.subreportConfigs.procedures" class="flex items-center space-x-2"><Checkbox id="mobile-sub-summary" v-model:checked="reportConfig.subreportConfigs.procedures.summaryBandEnabled"/><Label for="mobile-sub-summary" class="text-nova-dark">Podsumowanie podraportu</Label></div>
                      </div>
                      <Separator class="my-4"/>
                      <div v-if="reportConfig.companyInfo" class="space-y-2">
                        <div><Label for="mobile-company-name" class="text-nova-dark font-medium">Nazwa jednostki</Label><Input id="mobile-company-name" v-model="reportConfig.companyInfo.name" class="mt-1 border-nova-primary/30"/></div>
                        <div><Label for="mobile-company-address" class="text-nova-dark font-medium">Adres</Label><Input id="mobile-company-address" v-model="reportConfig.companyInfo.address" class="mt-1 border-nova-primary/30"/></div>
                        <div><Label for="mobile-company-postal" class="text-nova-dark font-medium">Kod i Miasto</Label><div class="flex gap-2 mt-1"><Input id="mobile-company-postal" v-model="reportConfig.companyInfo.postalCode" class="border-nova-primary/30"/><Input v-model="reportConfig.companyInfo.city" class="border-nova-primary/30"/></div></div>
                        <div><Label for="mobile-company-nip" class="text-nova-dark font-medium">NIP</Label><Input id="mobile-company-nip" v-model="reportConfig.companyInfo.taxId" class="mt-1 border-nova-primary/30"/></div>
                      </div>
                      <Separator class="my-4"/>
                      <div v-if="reportConfig.pageFooterEnabled" class="space-y-2">
                        <Label for="mobile-footer-text" class="text-nova-dark font-medium">Tekst w stopce</Label>
                        <textarea id="mobile-footer-text" v-model="reportConfig.footerLeftText" class="mt-1 w-full rounded-md border border-nova-primary/30 bg-background px-3 py-2 text-sm" rows="2"></textarea>
                      </div>
                    </AccordionContent>
                  </AccordionItem>
                  <AccordionItem value="item-2" class="border-nova-primary/20">
                    <AccordionTrigger class="text-base text-nova-dark hover:text-nova-primary">Kolumny</AccordionTrigger>
                    <AccordionContent class="pt-2">
                      <draggable v-model="reportConfig.columns" item-key="field" handle=".drag-handle" ghost-class="ghost-class" class="space-y-2">
                        <template #item="{ element: col }">
                          <div class="grid grid-cols-[auto_auto_1fr_auto] gap-2 items-center p-2 rounded-md border border-nova-primary/10 bg-nova-light/30">
                            <div class="drag-handle cursor-move p-2 text-nova-primary"><Icon name="grip" size="18"/></div>
                            <Checkbox v-model:checked="col.visible"/>
                            <div class="flex items-center space-x-2">
                              <Input v-if="!isSubreportColumn(col.field)" v-model="col.header" class="text-xs h-8"/>
                              <span v-else class="text-sm font-medium px-2">{{ availableFields.find(f => f.field === col.field)?.header }}</span>
                              <Button v-if="isSubreportColumn(col.field) && col.visible" @click="configureSubreport(col.field)" size="sm" variant="outline" class="h-8 px-2 text-xs"><Icon name="settings" size="12" class="mr-1"/>Konfig</Button>
                            </div>
                            <Input v-model.number="col.width" type="number" class="text-xs h-8 text-center w-20" placeholder="auto" :disabled="isSubreportColumn(col.field)"/>
                          </div>
                        </template>
                      </draggable>
                    </AccordionContent>
                  </AccordionItem>
                  <AccordionItem value="item-3" class="border-nova-primary/20">
                    <AccordionTrigger class="text-base text-nova-dark hover:text-nova-primary">Grupowanie</AccordionTrigger>
                    <AccordionContent class="pt-4 space-y-2">
                      <draggable v-model="reportConfig.groups" item-key="index" handle=".drag-handle" ghost-class="ghost-class" class="space-y-2">
                        <template #item="{ element: group, index }">
                          <div class="p-3 border rounded-md bg-nova-light/50 flex items-start space-x-2">
                            <div class="drag-handle cursor-move p-2 text-nova-primary"><Icon name="grip" size="18"/></div>
                            <div class="flex-grow w-full space-y-2">
                              <Label :for="`group-field-mob-${index}`" class="font-medium mb-2">Grupuj po polu</Label>
                              <Select v-model="group.field" :id="`group-field-mob-${index}`" class="mt-2">
                                <SelectTrigger class="w-full mt-1">
                                  <SelectValue placeholder="Wybierz pole..."/>
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem v-for="field in availableFields.filter(f => f.type !== 'subreport')" :key="field.field" :value="field.field">{{ field.header }}</SelectItem>
                                </SelectContent>
                              </Select>
                              <div class="flex justify-between items-center pt-2">
                                <div class="flex items-center space-x-2"><Checkbox :id="`group-footer-mob-${index}`" v-model:checked="group.showFooter"/><label :for="`group-footer-mob-${index}`" class="text-sm">Pokaż podsumowanie</label></div>
                                <Button variant="destructive" size="sm" @click="removeGroup(index)">Usuń</Button>
                              </div>
                            </div>
                          </div>
                        </template>
                      </draggable>
                      <Button @click="addGroup" class="mt-4 w-full" variant="outline"><Icon name="plus" class="mr-2" size="8"/>Dodaj grupę</Button>
                    </AccordionContent>
                  </AccordionItem>
                  <AccordionItem value="item-4" class="border-nova-primary/20">
                    <AccordionTrigger class="text-base text-nova-dark hover:text-nova-primary">Formatowanie</AccordionTrigger>
                    <AccordionContent class="pt-4 space-y-4">
                      <div class="flex items-center space-x-2"><Checkbox id="mobile-zebra" v-model:checked="reportConfig.formattingOptions.zebraStripes"/><Label for="mobile-zebra">Paski zebry</Label></div>
                      <div>
                        <Label for="mobile-theme" class="font-medium">Motyw</Label>
                        <Select v-model="reportConfig.theme" id="mobile-theme">
                          <SelectTrigger class="w-full mt-1">
                            <SelectValue placeholder="Wybierz motyw..."/>
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem v-for="theme in availableThemes" :key="theme.value" :value="theme.value">{{ theme.label }}</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <Separator />
                      <TabsRoot default-value="colors" class="w-full">
                        <TabsList class="grid w-full grid-cols-2"><TabsTrigger value="highlighting">Podświetlanie</TabsTrigger><TabsTrigger value="colors">Kolory</TabsTrigger></TabsList>
                        <TabsContent value="highlighting" class="mt-4">
                          <div v-for="(rule, index) in reportConfig.formattingOptions.highlightRules" :key="rule.id" class="p-3 border rounded-md mt-2 space-y-2 bg-nova-light/30">
                            <div class="flex flex-col space-y-2">
                              <span class="text-sm font-bold text-nova-primary">JEŚLI</span>
                              <Select v-model="rule.field">
                                <SelectTrigger>
                                  <SelectValue placeholder="Wybierz pole..."/>
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem v-for="field in availableFields" :key="field.field" :value="field.field">{{ field.header }}</SelectItem>
                                </SelectContent>
                              </Select>
                              <Select v-model="rule.operator">
                                <SelectTrigger>
                                  <SelectValue placeholder="Wybierz operator..."/>
                                </SelectTrigger>
                                <SelectContent>
                                  <SelectItem v-for="op in highlightOperators" :key="op.value" :value="op.value">{{ op.label }}</SelectItem>
                                </SelectContent>
                              </Select>
                              <template v-if="(availableFields.find(f => f.field === rule.field)?.type === 'numeric') && rule.operator !== 'CONTAINS'"><Input v.model="(rule as any).value" type="number" placeholder="Wartość liczbowa" class="w-full text-sm h-8"/></template>
                              <template v-else><Input v.model="(rule as any).value" placeholder="Wartość" class="w-full text-sm h-8"/></template>
                            </div>
                            <div class="flex items-center space-x-2 pt-2"><span class="text-sm font-bold text-nova-primary">WTEDY</span><Label for="color-picker-mob" class="text-sm">kolor tła:</Label><Input v.model="rule.color" type="color" id="color-picker-mob" class="h-8 w-12 p-1"/><div class="flex-grow"></div><Button variant="destructive" size="icon" @click="removeHighlightRule(index)"><Icon name="trash" size="14"/></Button></div>
                          </div>
                          <Button @click="addHighlightRule" class="mt-4 w-full" variant="outline"><Icon name="plus" class="mr-2" size="8"/>Dodaj regułę</Button>
                        </TabsContent>
                        <TabsContent value="colors" class="mt-4">
                          <div class="grid grid-cols-1 gap-y-2">
                            <div v-for="color in customColors" :key="color.key" class="grid grid-cols-2 items-center gap-2">
                              <Label class="text-sm text-right">{{ color.label }}</Label>
                              <input v.model="reportConfig.colorSettings[color.key]" type="color" class="h-8 w-12 p-1 border rounded-md" />
                            </div>
                          </div>
                        </TabsContent>
                      </TabsRoot>
                    </AccordionContent>
                  </AccordionItem>
                </Accordion>
              </ScrollArea>
            </div>


            <div class="flex-grow flex flex-col items-center justify-center bg-gray-200 dark:bg-gray-800 border-t border-nova-primary/20">
              <div v-if="reportLoading" class="flex flex-col items-center text-nova-primary">
                <LoaderCircle class="h-8 w-8 animate-spin mb-2"/>
                <p class="text-sm">Generowanie podglądu...</p>
              </div>
              <div v-else-if="pdfUrl" class="w-full h-full">
                <iframe :src="pdfUrl" :key="pdfUrl" class="w-full h-full border-0" title="Podgląd Raportu"></iframe>
              </div>
              <div v-else class="text-center text-nova-primary p-4">
                <p class="text-sm sm:text-base">Podgląd raportu pojawi się tutaj.</p>
              </div>
            </div>
          </div>

          <ResizableHandle with-handle class=" hidden lg:flex bg-nova-primary/10 hover:bg-nova-primary/20"/>
          <ResizablePanel :default-size="50" :min-size="30" class="hidden lg:block">
            <div class="flex flex-col h-full items-center justify-center">
              <div v-if="reportLoading" class="flex flex-col items-center text-nova-primary">
                <LoaderCircle class="h-8 w-8 animate-spin mb-2"/>
                <p class="font-medium">Generowanie podglądu...</p>
              </div>
              <div v-else-if="pdfUrl" class="w-full h-full p-1">
                <iframe :src="pdfUrl" class="w-full h-full  rounded-lg shadow-sm" title="Podgląd Raportu"></iframe>
              </div>
              <div v-else class="text-center text-nova-primary p-0">
                <div class="bg-white/80 p-6 rounded-lg border border-nova-primary/20 shadow-sm">
                  <p class="font-medium">Podgląd raportu pojawi się tutaj.</p>
                  <p class="text-sm mt-2 text-muted-foreground">Jeśli wystąpił błąd, sprawdź konsolę przeglądarki (F12).</p>
                </div>
              </div>
            </div>
          </ResizablePanel>
        </ResizablePanelGroup>
      </div>
      <Separator class="bg-nova-primary/20"/>
      <div class="mt-2 pt-2 border-nova-primary/20 flex flex-col sm:flex-row items-center justify-between space-y-2 sm:space-y-0  p-4 rounded-md">
        <Button @click="refreshPreview" :disabled="reportLoading" variant="outline"
                class="w-full sm:w-auto border-nova-accent text-nova-accent hover:bg-nova-accent hover:text-white">
          <LoaderCircle v-if="reportLoading" class="h-4 w-4 animate-spin" />
          {{ reportLoading ? 'Odświeżanie...' : 'Odśwież' }}
        </Button>
        <Button @click="downloadReport" :disabled="!pdfUrl || reportLoading"
                class="bg-nova-primary hover:bg-nova-dark text-white w-full sm:w-auto shadow-sm">
          <Icon name="download" class="mr-2" size="16"/>
          Pobierz PDF
        </Button>
      </div>
    </DialogContent>
  </Dialog>
  <Dialog :open="subreportEditorVisible" @update:open="subreportEditorVisible = $event">
    <DialogContent class="w-[95vw] sm:w-[90vw] lg:w-[70vw] xl:w-[60vw] max-w-none h-[90vh] sm:h-[80vh] lg:h-[70vh] flex flex-col bg-background border-nova-primary/20">
      <DialogHeader class="border-b border-nova-primary/10 pb-4">
        <DialogTitle class="text-lg sm:text-xl text-nova-dark font-semibold">Edytor Kolumn Subraportu: {{ editingSubreportField }}</DialogTitle>
        <DialogDescription class="text-sm sm:text-base text-muted-foreground">
          Przeciągnij, aby zmienić kolejność. Ustaw widoczność, nagłówki i szerokości kolumn.
        </DialogDescription>
      </DialogHeader>

      <div v-if="editingSubreportField && reportConfig.subreportConfigs[editingSubreportField]"
           class="flex-grow min-h-0 py-2 bg-nova-light/30">
        <ScrollArea class="h-full">
          <div class="grid grid-cols-3 sm:grid-cols-5 gap-2 sm:gap-8 items-center font-semibold text-sm mb-2 ml-4 sm:ml-20 text-nova-dark">
            <span class="text-center">Widoczna</span>
            <span class="col-span-1 sm:col-span-2 text-center">Nagłówek</span>
            <span class="text-center">Szer.</span>
          </div>
          <draggable v-model="reportConfig.subreportConfigs[editingSubreportField].columns"
                     item-key="field"
                     handle=".drag-handle"
                     ghost-class="ghost-class">
            <template #item="{ element: col }">
              <div class="grid grid-cols-4 sm:grid-cols-5 gap-x-1 sm:gap-x-2 gap-y-2 items-center mt-2 ml-2 sm:ml-4 p-2 bg-nova-light/50 border border-nova-primary/10 rounded-md">
                <div class="drag-handle cursor-move p-2 text-nova-primary hover:text-nova-accent">
                  <Icon name="grip" size="18"/>
                </div>
                <Checkbox v-model:checked="col.visible"
                          class="data-[state=checked]:bg-nova-accent data-[state=unchecked]:bg-background border-nova-primary/40 data-[state=checked]:border-nova-accent"/>
                <Input v-model="col.header" class="col-span-1 sm:col-span-2 text-xs h-8 border-nova-primary/30 focus:border-nova-accent"/>
                <Input v-model.number="col.width" type="number" class="text-xs h-8 text-center border-nova-primary/30 focus:border-nova-accent" placeholder="auto"/>
              </div>
            </template>
          </draggable>
        </ScrollArea>
      </div>

      <div class="mt-4 pt-4 border-t border-nova-primary/20 flex justify-end bg-nova-light/30 p-4 rounded-md">
        <Button @click="subreportEditorVisible = false" variant="secondary"
                class="w-full sm:w-auto bg-nova-light text-nova-dark hover:bg-nova-primary/10 border border-nova-primary/20">Zamknij</Button>
      </div>
    </DialogContent>
  </Dialog>
</template>

<style scoped>
.ghost-class {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>

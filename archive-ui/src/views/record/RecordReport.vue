<template>
  <div :style="{ position: 'relative', height: asideHeight + 'px' }">
    <split-pane
      split="vertical"
      @resize="resize"
      :min-percent="10"
      :default-percent="15"
    >
      <template slot="paneL">
        <el-container
          :style="{
            height: treeHeight + 'px',
            width: asideWidth,
            overflow: 'auto',
          }"
        >
          <el-tree
            :props="defaultProps"
            :data="treeData"
            node-key="id"
            style="width: 100%"
            default-expand-all
            :highlight-current="true"
            @node-click="handleNodeClick"
          ></el-tree>
        </el-container>
      </template>
      <template slot="paneR">
        <component :is="reportCompent"> </component>
      </template>
    </split-pane>
  </div>
</template>
<script>
import DataLayout from "@/components/ecm-data-layout";
import { workFlowMonth, avWorkFlow, srWorkFlow, otherFileFlow, designWorkFlow, commercialWorkFlow } from "./report/";
export default {
  name: "RecordReport",
  data() {
    return {
      // 本地存储高度名称
      leftStorageName: "PreArchiveftHeight",

      asideHeight: window.innerHeight - 100,
      treeHeight: window.innerHeight - 120,
      asideWidth: "100%",

      treeData: [
        {
          id: "1-0",
          label: "工作计量统计",
          component: "workFlowMonth",
          children: [
            {
              id: "1-1",
              label: "设计文件工作量统计",
              component: "designWorkFlow",
              children: "",
            },
            {
              id: "1-2",
              label: "商务文件工作量统计",
              component: "commercialWorkFlow",
              children: "",
            },
            {
              id: "1-3",
              label: "声像文件工作量统计",
              component: "avWorkFlow",
              children: "",
            },
            {
              id: "1-4",
              label: "科研文件工作量统计",
              component: "srWorkFlow",
              children: "",
            },
            {
              id: "1-5",
              label: "其他类型文件工作量统计",
              component: "otherFileFlow",
              children: "",
            },
          ],
        },
        {
          id: "2-0",
          label: "档案接收",
          component: "",
          children: "",
        },
        {
          id: "3-0",
          label: "档案升版",
          component: "",
          children: "",
        },
        {
          id: "4-0",
          label: "档案作废",
          component: "",
          children: "",
        },
        {
          id: "5-0",
          label: "非密档案利用情况",
          component: "",
          children: "",
        },
      ],

      defaultProps: {
        children: "children",
        label: "label",
      },

      reportCompent: "",
    };
  },
  components: {
    DataLayout: DataLayout,
    workFlowMonth: workFlowMonth,
    avWorkFlow: avWorkFlow,
    srWorkFlow: srWorkFlow,
    otherFileFlow: otherFileFlow,
    designWorkFlow: designWorkFlow,
    commercialWorkFlow: commercialWorkFlow,
  },

  created(){
    this.leftPercent = this.getStorageNumber(this.leftStorageName, 20);
  },

  methods: {
    resize(leftPercent) {
      // 左边百分比*100
      this.leftPercent = leftPercent;
      this.setStorageNumber(this.leftStorageName, leftPercent);
    },
    handleNodeClick(node, data) {
      this.reportCompent = node.component;
    },
  },
};
</script>
<style scoped>
</style>
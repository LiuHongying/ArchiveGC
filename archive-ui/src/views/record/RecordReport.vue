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
            :data="dataList"
            node-key="id"
            style="width: 100%"
            default-expand-all
            :highlight-current="true"
            @node-click="handleNodeClick"
          ></el-tree>
        </el-container>
      </template>
      <template slot="paneR"> </template>
    </split-pane>
  </div>
</template>
<script>
export default {
  data() {
    return {
      // 本地存储高度名称
      topStorageName: "ArchiveHandOverTopHeight",
      // 非split pan 控制区域高度
      startHeight: 140,
      // 顶部百分比*100
      topPercent: 65,
      // 顶部除列表高度
      topbarHeight: 35,
      // 底部除列表高度
      bottomHeight: 80,

      treeData: [
        {
          id: "1-0",
          label: "工作计量统计",
          component: "",
          children: [
            {
              id: "1-1",
              label: "设计文件工作量统计",
              component: "",
            },
            {
              id: "1-2",
              label: "商务文件工作量统计",
              component: "",
            },
            {
              id: "1-3",
              label: "声像文件工作量统计",
              component: "",
            },
            {
              id: "1-4",
              label: "科研文件工作量统计",
              component: "",
            },
            {
              id: "1-5",
              label: "其他类型文件工作量统计",
              component: "",
            },
          ],
        },
        {
          id: "2-0",
          label: "档案接收",
          component: "",
        },
        {
          id: "3-0",
          label: "档案升版",
          component: "",
        },
        {
          id: "4-0",
          label: "档案作废",
          component: "",
        },
        {
          id: "5-0",
          label: "非密档案利用情况",
          component: "",
        },
      ],

      defaultProps: {
        children: "children",
        label: "label",
      },
    };
  },

  methods: {
    onSplitResize(topPercent) {
      // 顶部百分比*100
      this.topPercent = topPercent;
      this.setStorageNumber(this.topStorageName, topPercent);
    },
    handleNodeClick() {},
  },
};
</script>
<style scoped>
</style>
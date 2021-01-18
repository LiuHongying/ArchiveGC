<template>
  <div>
    <button @click="printPage" v-print="'#print'">打印</button>
    <el-container style="width:100%;height:540px;overflow:auto;">
      <div id='print' ref='print' style="height:100%;width:100%;">
         <div v-for="(item,idx) in archiveObjects" :key="'divk'+idx" :style="'width:'+divWidth+';padding:5px;'">
           <PrintCoverpageForm :ref="'printForm'+idx" :archiveId="item.ID"></PrintCoverpageForm>
           <div v-if="idx < archiveObjects.length-1" style="page-break-before:always;"></div>
         </div>
      </div>
     </el-container>
  </div>
</template>

<script type="text/javascript">
import Print from "@/plugins/print";
import PrintCoverpageForm from '@/views/record/PrintCoverpageForm.vue'
import Vue from "vue";
Vue.use(Print);
export default {
  name: "PrintCoverpage",
  components: { 
     PrintCoverpageForm:PrintCoverpageForm
  },
  data() {
    return {
      archiveObjects: [],
      currentLanguage: "zh-cn",
    };
  },
  mounted() {
  
  },
  methods: {
    refreshArchiveObj(objs){
      let _self=this;
      this.archiveObjects = objs;
      if(objs){
         setTimeout(() => {
        for(var i=0;i<objs.length;i++){
          _self.$refs['printForm'+i][0].loadArchiveData(objs[i].ID);
        }},100);
      }
    },
    printPage(){
      this.$print(this.$refs.print)
    },
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>

<template>
  <div id="print" ref="print" style="width:100%;">
    <div style="height:85px">

    </div>
    <div class="topItem">
      424
    </div>
    <div class="topItem">
      {{archiveObject.C_ARCHIVE_CODING}}
    </div>
    <div class="topItem">
      {{archiveObject.C_ARC_CLASSIC}}
    </div>
    <div style="height:720px">

    </div>
    <div class="titleItem"> 
      {{archiveObject.TITLE}}
    </div>
    <div style="height:120px">

    </div>
    <div class="bottomItem"> 
      {{archiveObject.C_CREATE_UNIT}}
    </div>
    <div class="bottomItem"> 
      {{dateFormat(archiveObject.C_DRAFT_DATE)}}
    </div>
    <div class="bottomItem"> 
      {{archiveObject.C_RETENTION}}
    </div>
    <div class="bottomItem"> 
      {{archiveObject.C_SECURITY_LEVEL}}
    </div>
  </div>

</template>

<script type="text/javascript">
import Print from "@/plugins/print";
import Vue from "vue";

Vue.use(Print);
export default {
  name: "CoverpageForm",

  data() {
    return {
      archiveCode: "",
      archiveObject:[]
    };
  },
  mounted() {

  },
  props: {
    archiveId: { type: [String, Number] },
  },
  methods: {
    
    loadArchiveData(id) {
      let _self = this;
      var m = new Map();
      m.set("itemInfo", id); //ID 或类型
      m.set("lang", "zh-cn");
      _self
        .axios.post("/dc/getArchiveObj",JSON.stringify(m))
        .then(function(response) {
          _self.archiveObject = response.data.data.attributes;
        })
        .catch(function(error) {
          console.log(error);
        });
    },
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.topItem{
  text-align:left; 
  color: #000000;
  padding-top: 20px;
  padding-bottom: 10px;
  padding-left:330px;
  font-size:28pt;
}
.titleItem{
  line-height: 45px;
  height: 110px;
  text-align:left; 
  color: #000000;
  padding-left:450px;
  padding-right:150px;
  font-size:24pt;
  vertical-align: bottom;
  display: table-cell;
}
.bottomItem{
  text-align:left; 
  color: #000000;
  padding-top: 20px;
  padding-bottom: 24px;
  padding-left:450px;
  font-size:24pt;
}
</style>

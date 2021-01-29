<template>
  <div>
    <div id="print" ref="print" style="height:95%;width:95%;">
        <div style="text-align:center; font-size: 20px;font-family: Arial, 宋体, Helvetica, sans-serif;">备考表</div>
     <table class="table" style="border: 1px solid #000000;">
         <tr>
             <td>档案号：{{archiveCode}}</td>
         </tr>
        <tr style="border-top: 1px solid #000000;">
            <td>
                互见号：
                <br/>
                <br/>
                &nbsp;&nbsp;卷内情况说明：<br/><br/>
                &nbsp;&nbsp;&nbsp;&nbsp;本卷共有_________页； 图样______/_______页； 文件材料________/________
                <br/>
                <br/>
                <br/>
                页； 照片_________/________张。
                <br/><br/><br/><br/><br/><br/>
                其他情况；
                <br/><br/><br/><br/><br/><br/>
            </td>
            
        </tr>
        <tr style="border-top: 1px solid #000000;">
            <td style="text-align: center;">
                变更记录<br>
            </td>
        </tr>
        <tr style="border-top: 1px solid #000000;">
            <td>
                <table class="sub-table" cellspacing="0">
                    <tr>
                        <td rowspan="2" style="border-right: 1px solid #000000;text-align:center;border-bottom: 1px solid #000000;width:50px">序 号</td>
                        <td rowspan="2" style="border-right: 1px solid #000000;text-align:center;border-bottom: 1px solid #000000;">日 期</td>
                        <td rowspan="2" style="border-right: 1px solid #000000;text-align:center;border-bottom: 1px solid #000000;">原页数</td>
                        <td colspan="2" style="border-right: 1px solid #000000;text-align:center;border-bottom: 1px solid #000000;">变动页数</td>
                        <td rowspan="2" style="border-right: 1px solid #000000;text-align:center;border-bottom: 1px solid #000000;">现页数</td>
                        <td rowspan="2" style="border-right: 1px solid #000000;text-align:center;border-bottom: 1px solid #000000;">更改文件号</td>
                        <td rowspan="2" style="border-right: 1px solid #000000;text-align:center;border-bottom: 1px solid #000000;">更改者</td>
                        <td rowspan="2" style="text-align:center;border-bottom: 1px solid #000000;">备注</td>
                    </tr>
                    <tr>
                        <td style="text-align:center;border-right: 1px solid #000000;border-bottom: 1px solid #000000;">增 加</td>
                        <td style="border-right: 1px solid #000000; text-align:center;;border-bottom: 1px solid #000000;">减 少</td>
                    </tr>
                     <tr v-for="idx in emptyRowCount" :key="idx">
                        <td class="sub-table-tdNull bottom-cell" >&nbsp;</td>
                        <td class="sub-table-tdNull bottom-cell">&nbsp;</td>
                        <td class="sub-table-tdNull bottom-cell">&nbsp;</td>
                        <td class="sub-table-tdNull bottom-cell">&nbsp;</td>
                        <td class="sub-table-tdNull bottom-cell">&nbsp;</td>
                        <td class="sub-table-tdNull bottom-cell">&nbsp;</td>
                        <td class="sub-table-tdNull bottom-cell">&nbsp;</td>
                        <td class="sub-table-tdNull bottom-cell">&nbsp;</td>
                        <td class="sub-table-tdNull bottom-cell-end">&nbsp;</td>
                    </tr>
                    
                </table>
                <table class="sub-table" cellspacing="0">
                    <tr class="sub-table-tr">
                       <td class="sub-table-tdNull" style="padding:5px;width:12.5%;border-right: 1px solid #000000;text-align:center;">立卷人</td>
                       <td class="sub-table-tdNull" style="width:12.5%;border-right: 1px solid #000000;text-align:center;"></td>
                       <td class="sub-table-tdNull" style="padding:5px;width:12.5%;border-right: 1px solid #000000;text-align:center;">立卷日期</td>
                       <td class="sub-table-tdNull" style="width:12.5%;border-right: 1px solid #000000;text-align:center;"></td>
                       <td class="sub-table-tdNull" style="width:12.5%;border-right: 1px solid #000000;text-align:center;">检查人</td>
                       <td class="sub-table-tdNull" style="width:12.5%;border-right: 1px solid #000000;text-align:center;"></td>
                       <td class="sub-table-tdNull" style="width:12.5%;border-right: 1px solid #000000;text-align:center;">检查日期</td>
                       <td class="sub-table-tdNull" style="border-right:0px;width:12.5%;text-align:center;"></td>
                    </tr>
                </table>
            </td>
        </tr>
     </table>
      
    </div>
  </div>
</template>

<script type="text/javascript">
import Print from "@/plugins/print";
import Vue from "vue";

Vue.use(Print);
export default {
  name: "PreparationTableForm",

  // name: "printPage",
  data() {
    return {
      archiveCode: "",
      innerDataList: [],
      emptyRowCount: 30
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
          _self.archiveCode = response.data.data.attributes.C_ARCHIVE_CODING;
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
h1,
h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
.bottom-cell {
  border-bottom:1px solid #000000;
  border-right:1px solid #000000;
  padding:10px;
}
.bottom-cell-end {
  border-bottom:1px solid #000000;
  padding:10px;
}
/* .table-a table{border:1px solid #000000}  */
.v-auto-out .auto-in {
position: absolute;
top: 50%;
border: 1px dashed #ddd;
/* 这里有兼容性问题 */
-webkit-transform: translateY(-50%);
-ms-transform: translateY(-50%);
-o-transform: translateY(-50%);
transform: translateY(-50%);
}

  .bg-purple-dark {
    background: #99a9bf;
  }
  .bg-purple {
    background: #d3dce6;
  }
  .bg-purple-light {
    background: #e5e9f2;
  }
  .grid-content {
    border-radius: 4px;
    min-height: 36px;
  }
  .row-bg {
    padding: 10px 0;
    background-color: #f9fafc;
  }
/* 样式 */
  .table, .table * {margin: 0 auto; padding: 0;font-size: 14px;
  font-family: Arial, 宋体, Helvetica, sans-serif;}   
.table {display: table; width: 80%; border-collapse: collapse;/*border-bottom: 1px solid gray;*/}   
  
.table-tr {display: table-row; height: 30px;}   
.table-th {display: table-cell;font-weight: bold;height: 100%;/*border: 1px solid gray;*/text-align: center;vertical-align: middle;}   
.table-td {display: table-cell; height: 100%;}   
  
.sub-table {width: 100%;height: 100%;display: table;}   
.sub-table-tr {display: table-row; height: 100%;}   
.sub-table-td {display: table-cell; height: 100%;
/* border-top: 1px solid gray; 
border-left: 1px solid gray;
border-right: 1px solid gray; */
border-top: 1px solid #000000; 
border-left: 1px solid #000000;
border-right: 1px solid #000000;
/* border:1px solid #000000; */
text-align: center;vertical-align: middle;}
.sub-table-td1 {display: table-cell; height: 100%;
/* border-left: 1px solid #000000; */
text-align: center;vertical-align: middle;}
</style>

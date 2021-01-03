<template>
  <div>
      <!-- <div style="width:30%;display:inline-block;position: absolute; left:30px;">
        <el-select
            name="selectName"
            v-model="selectedPx"
            placeholder="'请选择条码尺寸'"
            style="display:block;"
            @change="refresh(archiveObjects,selectedPx)"
            >
            <el-option label="1条码" value="1" key='2'></el-option>
            <el-option label="2条码" value="2" key='2'></el-option>
            <el-option label="3条码" value="3" key='3'></el-option>
            <el-option label="4条码" value="4" key='4'></el-option>
            <el-option label="5条码" value="5" key='5'></el-option>
            <el-option label="6条码" value="6" key='6'></el-option>
          </el-select>
          
      </div> -->
      <div style="display:inline-block;position: absolute;left:480px;">
        <button @click="printCode" v-print="'#print'">打印</button>
      </div>
      <div id='print' ref='print' :style="'position: absolute; top:0px;'">
        <div v-for="(item,keys) in getArchiveObjs()" :key="'divk'+keys" style="width:400px">
          <el-row>
            <el-col :span="12" style="color: #000000;text-align: left;font-size:18px;padding:2px;">{{item.TYPE_NAME}}</el-col>
            <el-col :span="6" style="color: #000000;text-align: left;font-size:18px;padding:2px;">案卷</el-col>
            <el-col :span="6" style="color: #000000;text-align: left;font-size:18px;padding:2px;">复制件</el-col>
          </el-row>
          <el-row>
            <el-col :span="24" style="color: #000000;text-align: left;font-size:32px;padding:2px;">{{item.CODING}}&nbsp;111111</el-col>
          </el-row>
          <el-row>
            <el-col :span="12" style="color: #000000;text-align: left;font-size:18px;padding:2px;">密级：{{item.C_SECURITY_LEVEL}}</el-col>
            <el-col :span="12" style="color: #000000;text-align: left;font-size:18px;padding:2px;">版本：{{item.REVISION}}</el-col>
          </el-row>
          <el-row>
            <el-col :span="12" style="color: #000000;text-align: left;font-size:18px;padding:2px;">保管期限：{{item.C_RETENTION}}</el-col>
            <el-col :span="12" style="color: #000000;text-align: left;font-size:18px;padding:2px;">工程号：{{item.C_PROJECT_NUM}}</el-col>
          </el-row>
          <el-row>
            <el-col :span="24" style="color: #000000;text-align: left;font-size:18px;padding:2px;">归档日期：{{dateFormat(item.C_ARCHIVE_DATE)}}</el-col>
          </el-row>
          <el-row style="padding-bottom:5px;">
            <el-col :span="10" style="color: #000000;font-size:46px;padding-top:10px;">
              <el-row style="color: #000000;text-align: left;font-size:18px;padding:2px;"> </el-row>
              <el-row>{{item.C_STORE_CODING}}</el-row>
            </el-col>
            <el-col :span="14"><canvas :ref="'canvas'+keys" :style="'display:'+noneStr"></canvas><img :ref="'image'+keys" /></el-col>
          </el-row>
        </div>
        <!-- <div v-if="isQRCode"  ref='qrCodeUrl2'></div> -->
  　　</div>
  </div>
</template>

<script type="text/javascript">
import Print from '@/plugins/print'
import PDF417 from '@/plugins/pdf417'
import Vue from 'vue';
import QRCode from 'qrcodejs2'// 引入qrcode
import JsBarcode from 'jsbarcode'
Vue.use(Print)
Vue.use(PDF417);
export default {
   name: 'printArchiveCode',
    
  // name: "printPage",
  data() {
    return {
      archiveTitle:"",
      archiveCode:"",
      innerDataList:[],
      dialogQrcodeVisible: true,
      currentLanguage: "zh-cn",
      volumeTitle:"",
      ridgeData:[],
      selectedPx:'112',
      noneStr:"block",
      barCodeWidth:2,
      barCodeHeight:40,
    };
  },
  mounted() {
    // 需要先显示出来，然后再隐藏掉；  否则动态生成的二维码，第一次会报错，对象找不到。可能是跟初始化有关系，没有显示出来的时候并没有初始化HTML
      // this.dialogQrcodeVisible = false
    this.currentLanguage = localStorage.getItem("localeLanguage") || "zh-cn";
    this.getConfigParam("PrintArchiveCodeConfig");
    // this.loadFormInfo();
    // this.getArchiveObj(this.archiveId,this.gridName); 
    
  },
  props: {
    archiveId: {type:[String,Number]},
    currentFolderId:{type:[String,Number]},
    tableHeight:{type:Number},
    gridName:{type:String},
    isQRCode:{type:Boolean,default:false},
    isBarCode:{type:Boolean,default:false},
    archiveObjects:{type:Array,default:() => []},
    divWidth:{type:String,default:'400px'},
    divHeight:{type:String,default:'50px'},
    divMargin:{type:String,default:'10px'},
  },
  methods: {
      getConfigParam(keyName) {
        let _self = this;
        axios
          .post("/dc/getJsonParamMap", keyName)
          .then(function(response) {
            _self.divWidth = response.data.data.divWidth;
            _self.divHeight = response.data.data.divHeight;
            _self.divMargin = response.data.data.divMargin;
          })
          .catch(function(error) {
            console.log(error);
          });
      },
      getArchiveObjs(){
        return this.archiveObjects;
      },
      refresh(objs,pixel){
        let _self=this;
        _self.archiveObjects=objs;
        
        for(let i=0;i<objs.length;i++){
          let obj=objs[i];
          _self.generate(obj["CODING"],_self.$refs['canvas'+i][0]);
        }
        
      },

    getArchiveObj(id,volumeTitle){
      let _self=this;
      _self.volumeTitle=volumeTitle;
      var m = new Map();
      m.set('itemInfo',id);//ID 或类型
      m.set('lang',_self.currentLanguage);
      _self.axios({
          headers: {
            "Content-Type": "application/json;charset=UTF-8"
          },
          method: "post",
          data: JSON.stringify(m),//_self.myItemId+_self.myTypeName,
          url: "/dc/getArchiveObj"
        })
        .then(function(response) {
          _self.ridgeData=response.data.data;
          _self.archiveCode= response.data.data.coding;
          _self.archiveTitle= response.data.data.title;
          // _self.genarateQrcode(_self.archiveCode);
          _self.genarateBarCode('#barcode0',_self.archiveCode,_self.barCodeWidth,_self.barCodeHeight);
          // _self.InnerFile();
          //console.log(JSON.stringify(response.data.data));
          _self.loading = false;
        })
        .catch(function(error) {
          console.log(error);
          _self.loading = false;
        });
    },
    printCode(){
      this.noneStr = "none";
      if(this.archiveObjects && this.archiveObjects.length>0){
        for(var i=0; i<this.archiveObjects.length; i++){
          this.$refs["image"+i][0].src = this.$refs['canvas'+i][0].toDataURL();
        }
      }
      setTimeout(() => {
      this.$print(this.$refs.print);
    }, 200);
    },
    
    generate(content,showCanvas) {
        this.PDF417.init(content);             

        let barcode = this.PDF417.getBarcodeArray();

        // block sizes (width and height) in pixels
        let bw = 2;
        let bh = 2;

        // create canvas element based on number of columns and rows in barcode
        

        let canvas = showCanvas;
        canvas.width = bw * barcode['num_cols'];
        canvas.height = bh * barcode['num_rows'];
        
        let ctx = canvas.getContext('2d');                    

        // graph barcode elements
        let y = 0;
        // for each row
        for (let r = 0; r < barcode['num_rows']; ++r) {
            let x = 0;
            // for each column
            for (let c = 0; c < barcode['num_cols']; ++c) {
                if (barcode['bcode'][r][c] == 1) {                        
                    ctx.fillRect(x, y, bw, bh);
                }
                x += bw;
            }
            y += bh;
        }       
    }
    
    
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style media="printContent" scoped>
@page {
size: auto; /* auto is the initial value /
margin: 3mm; / this affects the margin in the printer settings */
}

html {
background-color: #ffffff;
margin: 0px; /* this affects the margin on the html before sending to printer */
}

body {
border: solid 1px rgba(255,255,255,0);
/* margin: 10mm 15mm 10mm 15mm; margin you want for the content */
}
</style>

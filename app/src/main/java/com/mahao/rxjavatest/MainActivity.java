package com.mahao.rxjavatest;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.rxjava_img)
    private ImageView imageview;
    private Observable<String> mStringObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ShowDraw();

        //个人感觉: map和flatMap没有什么区别,map主要是处理原始数据,返回其他类型数据
        // flatMap 主要是对Obseravle进行处理,返回原始的类型或者其他类型数据;
    }

    private void ShowDraw() {

        final ArrayList<Integer> list  = new ArrayList() ;
        list.add(R.mipmap.ic_launcher);
        list.add(R.mipmap.ic_launcher);
        list.add(R.mipmap.ic_launcher);

        Observable.create(new Observable.OnSubscribe<ArrayList<Integer>>() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void call(Subscriber<? super ArrayList<Integer>> subscriber) {

                for(int i = 0; i < list.size(); i++){

                    Drawable drawable = getTheme().getDrawable(list.get(i));

                    //subscriber.onNext(drawable);
                }
            }
        });
    }

    //最基本的展现形式
    private void showStringTest(){


        //事件源,被观察者
        Observable<String> stringObservable = Observable.create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(Subscriber<? super String> subscriber) {

                subscriber.onNext("hello world!");
                subscriber.onCompleted();
            }
        });

        //观察者
        Subscriber<String> subscriber = new Subscriber<String>() {


            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {

                System.out.print(s);
            }
        };

        //这样就完成了subscriber对stringObservable的订阅
        stringObservable.subscribe(subscriber);
    }



    //简化形式  操作符 just就是用来创建只发出一个事件结束的Observable对象.
    public void showStringTest2(){


        Observable<String> stringObservable = Observable.just("hello world!");

        //不用关心error 和 complete,就使用Action

        Action1<String> onNextAction = new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.print(s);
            }
        };
        stringObservable.subscribe(onNextAction);
    }


    //简化形式  map就是为了解决Observable对象的变换的问题
    public void showStringTest3(){


        Observable.just("hell,world").map(new Func1<String, Integer>() {

            @Override
            public Integer call(String s) {

                return s.hashCode();

            }
        }).subscribe(new Action1<Integer>() {


            @Override
            public void call(Integer integer) {

                System.out.println(integer);
            }
        });
    }


    // 继续使用map进行Obserable的转化
    public void showTest4(){

        Observable.just("hello world").map(new Func1<String, Integer>() {
            @Override
            public Integer call(String s) {

                return s.hashCode();
            }
        }).map(new Func1<Integer, String>() {
            @Override
            public String call(Integer integer) {

                String s = integer.toString();
                return s;
            }
        });
    }

    //使用from 接收一个集合作为输入,然后每次输出一个元素给subscriber;
    public void showStringTest5(){

        Observable.from(new String[]{"url1","url2","url3"}).map(new Func1<String,Integer>() {

            @Override
            public Integer call(String s) {

                return s.hashCode();
            }
        }).subscribe(new Action1<Integer>() {

            @Override
            public void call(Integer integer) {

                System.out.println(integer.toString());
            }
        });

    }


    //操作符号  flatMap:接收一个Observable输出作为输入,同时输出另外一个Observable.
    public void showStringTest6(){

        final HashMap<String,String> map = new HashMap();
        final List<HashMap<String,String>> list = new ArrayList<>();
        map.put("mahao","1000yi");
        map.put("zhangsan","1000wang");
        list.add(map);

        Observable.from(new String[]{"abc","bca","cba"})

                .flatMap(new Func1<String, Observable<List<String>>>() {

            @Override
            public Observable<List<String>> call(String s) {

                List mlist = new ArrayList();
                mlist.add(map.get("mahao"));
                mlist.add(map.get("zhangsan"));
                return Observable.from(mlist);
            }
        }).map(new Func1<List<String>, Observable<List<Integer>>>() {

            //注意  from操作符 : 是从集合中获取的对象 作为 Observable的返回值;
            @Override
            public Observable<List<Integer>> call(List<String> strings) {

                List<Integer> mlist = new ArrayList<Integer>();
                List<List<Integer>>  listNew = new ArrayList<List<Integer>>();
                for(int i = 0 ; i < strings.size() ; i++){

                    mlist.add(strings.get(i).hashCode());
                }
                listNew.add(mlist);
                return Observable.from(listNew);   // 这里可以看出from和just的区别;

            }
        }).map(new Func1<Observable<List<Integer>>, Observable<List<Integer>>>() {

            @Override
            public Observable<List<Integer>> call(Observable<List<Integer>> listObservable) {


                final List<Integer> mlist = new ArrayList<Integer>();
                listObservable.subscribe(new Action1<List<Integer>>() {
                    @Override
                    public void call(List<Integer> integers) {

                        for(int i = 0 ; i < integers.size(); i++){

                            mlist.add(integers.get(i).hashCode());
                        }
                    }
                });
                return Observable.just(mlist);  //Just是提交当前的对象,from是遍历集合内部对象作为输入
            }
        }).map(new Func1<Observable<List<Integer>>, String>() {

            @Override
            public String call(final Observable<List<Integer>> listObservable) {

                final StringBuilder str = new StringBuilder();

                //将list<inter> 转成 Integer 对象
                listObservable.map(new Func1<List<Integer>, Observable<Integer>>() {

                    List<Integer> list = new ArrayList<Integer>();
                    @Override
                    public Observable<Integer> call(List<Integer> integers) {

                        for(int i = 0; i < integers.size() ; i++){

                            list.add(integers.get(i));
                        }
                        return Observable.from(list);
                    }
                    // 将Integer对象转成 String 对象
                }).map(new Func1<Observable<Integer>,String>() {

                    @Override
                    public String call(Observable<Integer> integerObservable) {

                        final StringBuilder sb = new StringBuilder();

                        integerObservable.map(new Func1<Integer,String>() {
                            @Override
                            public String call(Integer integer) {

                                return integer.toString();
                            }
                        }).subscribe(new Action1<String>() {

                            @Override
                            public void call(String s) {

                                sb.append(s.toString());

                            }
                        });
                        return sb.toString();
                    }
                }).subscribe(new Action1<String>() {

                    @Override
                    public void call(String s) {

                        str.append(s);
                    }
                });
                return str.toString();
            }
        }).subscribe(new Action1<String>() {

            @Override
            public void call(String s) {

                System.out.print(s);

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

                throwable.printStackTrace();
            }
        });
    }

    // take(4) 只显示4个;  filter 过滤;
     public void showTestString6(){

         String[] aa = new String[]{"abc","abcd","","aabb","cc","ff"};
         Observable.just(aa).filter(new Func1<String[], Boolean>() {

             @Override
             public Boolean call(String[] strings) {

                 for(int i = 0 ; i < strings.length ; i++){

                     if(strings.equals(null)){

                         return false;
                     }
                 }
                 return true;
             }
         }).take(4).map(new Func1<String[], Integer>() {

             @Override
             public Integer call(String[] strings) {

                 int hashCode = 0;
                 for(int i = 0; i < strings.length ; i++){

                     hashCode += strings[i].hashCode();
                 }
                 return hashCode;
             }
         }).subscribe(new Action1<Integer>() {

             @Override
             public void call(Integer integer) {

                 System.out.println(integer.toString());
             }
         });
     }


    // doOnnext 允许我们在输出一个数字的时候去做另外的一些事情;
    public  void showTest7(){

        List<HashMap<String,String>> mlist = new ArrayList<>();
        HashMap<String,String> map = new HashMap<>();
        map.put("1","mahao");
        map.put("2","zhangsan");
        map.put("3","lisi");

        mlist.add(map);
        Observable.from(mlist).map(new Func1<HashMap<String,String>,List<String>>() {

            List<String> mlist = new ArrayList<String>();
            @Override
            public List<String> call(HashMap<String, String> stringStringHashMap) {

                Set<String> strings = stringStringHashMap.keySet();
                Iterator<String> it = strings.iterator();
                while (it.hasNext()){

                    String key = it.next();
                    String value = stringStringHashMap.get(key);
                    mlist.add(value);
                }
                return mlist;
            }
        }).doOnNext(new Action1<List<String>>() {

            @Override
            public void call(List<String> strings) {

                //保存数据包本地;
            }
        }).subscribe(new Action1<List<String>>() {
            @Override
            public void call(List<String> strings) {

                for(int i = 0 ; i < strings.size() ; i ++){

                    System.out.print(strings.get(i).hashCode());
                }
            }
        });
    }


    //由subscribeOn()结合observeon()l爱实现线程控制,让事件的产生和消费发生在不同的线程
    public void showTestString7(){


        Observable.just(1,2,3,4)
                .subscribeOn(Schedulers.io())  //由观察者指定事件源运行的线程
                .observeOn(Schedulers.newThread())   //指定
                .map(new Func1<Integer, String>() {
                    @Override
                    public String call(Integer integer) {
                        return null;
                    }
                }).observeOn(Schedulers.io())
                .map(new Func1<String,String>() {
                    @Override
                    public String call(String s) {
                        return null;
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        System.out.print(s);
                    }
                });
    }


    /* compose()操作符号  参考: RxFace,http://www.jianshu.com/p/e9e03194199e
    *
    *   1 : 是一个唯一能从流中获取原声的Observable的方法, 操作整个流;
    *
    *   2 : compose 主要不会打断Rxjava的链式的结构,能够一直链接;
    *
    *   3 : 个人理解: 因为像 Retrofit 调用的时候,我们可能会将Obserable作为参数传递
    *        打断了Rxjava的链式,因此使用compose ,再次获取Obserable:
    *
    *   4 : 直接原因: 因为类似applySchedulers()导致了它不再是操作符，因此很难在其后面追加其他操作符
    * */

    // 第一步,确定转换器  compose(Transformer)
    public class LiftAllTransformer implements Observable.Transformer {

        @Override
        public Object call(Object observable) {
            return ((Observable) observable).subscribeOn(Schedulers.from(ExecutorManager.eventExecutor))
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    //调用Retrofit请求框架,使用compose,然后生成Obserable;
   public Observable getImageUrl(){

       mStringObservable = Observable.just("abc").map(new Func1<String, String>() {
           @Override
           public String call(String s) {
               return null;
           }
       }).compose(new LiftAllTransformer());   // compose操作获取了当前流的Obserable;
       return mStringObservable;
   }

    //第三步 : 完成链式的展示
    public void showTestString8(){

        mStringObservable.map(new Func1<String,Integer>() {
            @Override
            public Integer call(String s) {
                return s.hashCode();
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {

                System.out.println(integer.toString());
            }
        });
    }
}














